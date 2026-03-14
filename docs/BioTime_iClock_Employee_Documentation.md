# BioTime -> Spring Boot (zkteco) Documentation

## 1. Purpose and Scope
This document explains the employee and iClock communication modules migrated from the legacy BioTime application into the Spring Boot app `zkteco`.

Focus area:
- Employee domain (list/create/update/delete + device actions)
- iClock device communication protocol (`cdata`, `getrequest`, `devicecmd`, `ping`)
- Device command queue and command result flow
- Attendance transaction upload flow
- Async action/job flow

Not in this phase:
- Full legacy feature parity for every module (visitor, payroll, meeting, etc.)
- Every biometric command variant from compiled device handlers

## 2. Source Systems Covered

### 2.1 Legacy BioTime (Django, no Laravel)
Main route sources reviewed:
- `iclock/urls.py`
- `iclock/api/urls.py`
- `personnel/urls.py`
- `personnel/api/urls.py`
- `iclock/devview_ex.py`
- `iclock/comm/const.py`
- `iclock/comm/utils.py`
- `personnel/models/model_employee.py`
- `iclock/models/model_terminal.py`
- `iclock/models/model_transaction.py`

Important note:
- Production device handler is in `iclock/comm/devview.pyd` (compiled). Some behavior is inferred using `devview_ex.py`, constants, models, and command traces.

### 2.2 Spring Boot `zkteco`
Main implementation sources:
- `zkteco/src/main/java/Zkteco/zkteco/web/IclockController.java`
- `zkteco/src/main/java/Zkteco/zkteco/modules/iclock/*`
- `zkteco/src/main/java/Zkteco/zkteco/modules/employee/*`
- `zkteco/src/main/java/Zkteco/zkteco/modules/job/*`
- Flyway migrations `V1__init_employee_iclock.sql`, `V2__async_jobs.sql`

## 3. Legacy BioTime Endpoint Map (Employee + iClock)

### 3.1 Device Push/Pull Endpoints (Legacy)
Base: `/iclock`
- `GET/POST /iclock/cdata`
  - Device uploads data (`ATTLOG`, `OPLOG`, user/bio payloads).
- `GET /iclock/getrequest`
  - Device asks server for next command.
- `GET/POST /iclock/devicecmd`
  - Device command polling and command result callback.
- `GET /iclock/fdata`
  - File/photo related endpoint.
- `GET /iclock/ping`
  - Health endpoint for device communication.

### 3.2 Legacy iClock API Endpoints (Django REST)
Base: `/iclock/api`
- `terminals`
- `transactions`
- `terminalcommands`
- `pendingcommands`
- `terminallogs`
- `terminaluploadlogs`
- `biodatas`
- `device_config`
- `bio_photo`
- ...and other message/workcode endpoints

### 3.3 Legacy Personnel API Endpoints (Django REST)
Base: `/personnel/api`
- `employees`, `employee`
- `department`, `departments`
- `position`, `positions`
- `areas`, `areagroups`
- `company`
- `resigns`
- `employee_certifications`

## 4. Spring Boot Route Map (Current)

### 4.1 iClock Communication Endpoints
Base: `/iclock`
- `GET /iclock/ping`
- `GET /iclock/cdata`
- `POST /iclock/cdata`
- `GET /iclock/getrequest`
- `GET /iclock/devicecmd`
- `POST /iclock/devicecmd`
- `GET /iclock/fdata`

### 4.2 iClock Management API
Base: `/api/v1/iclock`
- `GET /api/v1/iclock/devices`
- `GET /api/v1/iclock/terminals`
- `GET /api/v1/iclock/terminals/{sn}/commands`
- `POST /api/v1/iclock/commands`

### 4.3 Employee API
Base: `/api/v1/employees`
- `GET /api/v1/employees`
- `GET /api/v1/employees/{id}`
- `POST /api/v1/employees`
- `PUT /api/v1/employees/{id}`
- `DELETE /api/v1/employees/{id}`

### 4.4 Employee Device Action API (Async)
Base: `/api/v1/employees`
- `POST /api/v1/employees/{id}/actions/sync-to-device`
- `POST /api/v1/employees/{id}/actions/pull-from-device`
- `POST /api/v1/employees/actions/sync-batch-to-device`
- `POST /api/v1/employees/actions/resync-all`

### 4.5 Async Job API
Base: `/api/v1/jobs`
- `GET /api/v1/jobs/{id}`

## 5. Data Communication Protocol (iClock)

### 5.1 Device -> Server: cdata
Typical query parameters:
- `SN`: serial number (required)
- `table`: payload type, e.g. `ATTLOG`, `OPLOG`
- Optional metadata like `DeviceType`, `pushver`, `INFO`

Behavior in Spring:
1. Resolve/register terminal by `SN`.
2. Update terminal heartbeat/activity/IP/product metadata.
3. If `table=ATTLOG`, parse and store attendance rows.
4. If `table=OPLOG` or command result payload, update command result.
5. Return `OK` or `OK:<count>`.

### 5.2 Device -> Server: getrequest
Typical query parameters:
- `SN`

Behavior:
1. Resolve terminal.
2. Fetch oldest pending command (`transfer_time IS NULL AND return_time IS NULL`).
3. Mark `transfer_time=now`.
4. Return command in format:
   - `C:<commandId>:<commandContent>`
5. If none pending: `OK`.

### 5.3 Device -> Server: devicecmd
Used for command status callback and/or polling.

Behavior:
- If result data exists (`ID`, `CmdId`, `Return`, body kv): update matching command with `return_value`, `return_time`.
- If GET without result data, server may fallback to command pull behavior.

## 6. Supported Command Patterns

### 6.1 Commands pushed by Spring in current module
- Upload employee basic data:
  - `DATA USER PIN=<empCode>\tName=<firstName>\tPasswd=<pwd>\tCard=<card>\tPri=<priv>\tVerify=<verifyMode>`
- Query one employee from device:
  - `DATA QUERY USERINFO PIN=<empCode>`

### 6.2 Legacy BioTime command patterns (observed)
- `INFO`
- `CHECK ALL`
- `SET OPTION <key>=<value>`
- `DATA UPDATE USERINFO ...`
- `DATA FP ...`
- `DATA UPDATE FINGERTMP ...`
- `DATA UPDATE FACE ...`
- `DATA UPDATE BIODATA ...`
- `DATA UPDATE USERPIC ...`
- `DATA UPDATE BIOPHOTO ...`
- `DATA QUERY ATTLOG StartTime=... EndTime=...`
- `DATA DELETE FINGERTMP ...`, `DATA DEL_FP ...`
- `ENROLL_FP ...`, `ENROLL_BIO ...`, etc.

## 7. Attendance Upload Format

### 7.1 ATTLOG row format used by parser
Tab-separated columns expected:
1. `pin`
2. `punch_time` (`yyyy-MM-dd HH:mm:ss` or `time,lon,lat`)
3. `punch_state`
4. `verify`
5. `work_code`
6. `reserved`
7. `reserved_02`
8. `mask_flag` (optional)
9. `temperature` (optional)

### 7.2 Transaction write behavior
Saved fields include:
- `company_code`, `emp_code`, `emp_id`
- `punch_time`, `punch_state`, `verify_type`
- `terminal_sn`, `terminal_alias`, `terminal_id`
- `area_alias`, `longitude`, `latitude`
- `source=1`, `sync_status=0`
- `is_mask`, `temperature`

Duplicate protection:
- Unique key on `(company_code, emp_code, punch_time)`.

## 8. Employee-to-Device Action Flows (Async)

### 8.1 Sync one employee to device
Endpoint:
- `POST /api/v1/employees/{id}/actions/sync-to-device`

Request body (optional):
```json
{
  "terminalSns": ["A1234567", "B998877"]
}
```

If `terminalSns` omitted:
- Worker resolves terminals by employee areas, fallback all terminals.

### 8.2 Pull one employee from device
Endpoint:
- `POST /api/v1/employees/{id}/actions/pull-from-device`

Queues command:
- `DATA QUERY USERINFO PIN=<empCode>`

### 8.3 Sync batch employees
Endpoint:
- `POST /api/v1/employees/actions/sync-batch-to-device`

Request:
```json
{
  "employeeIds": [1,2,3],
  "terminalSns": ["A1234567"]
}
```

If `employeeIds` omitted, all employees are used.

### 8.4 Resync all employees
Endpoint:
- `POST /api/v1/employees/actions/resync-all`

Request:
```json
{
  "terminalSns": ["A1234567", "B998877"]
}
```

If `terminalSns` omitted, all terminals are targeted.

### 8.5 Async job lifecycle
Status values:
- `PENDING`
- `RUNNING`
- `SUCCESS`
- `FAILED`

Job check:
- `GET /api/v1/jobs/{id}`

## 9. Example API Calls

### 9.1 Create employee
`POST /api/v1/employees`
```json
{
  "empCode": "10001",
  "firstName": "Ali",
  "lastName": "Khan",
  "companyId": 1,
  "departmentId": 1,
  "positionId": 1,
  "areaIds": [1],
  "cardNo": "123456",
  "devicePassword": "1234",
  "devPrivilege": 0,
  "verifyMode": 0,
  "enablePayroll": true,
  "email": "ali@example.com"
}
```

### 9.2 Queue direct terminal command
`POST /api/v1/iclock/commands`
```json
{
  "sn": "A1234567",
  "content": "INFO"
}
```

### 9.3 Device polling command
`GET /iclock/getrequest?SN=A1234567`

Response examples:
- `C:105:INFO`
- `OK`

### 9.4 Device posting attendance
`POST /iclock/cdata?SN=A1234567&table=ATTLOG`
Body:
```text
10001	2026-02-25 08:00:01	0	1				1	36.4
```
Response:
```text
OK:1
```

## 10. Database Objects (Current Spring)

### 10.1 Core tables
- `personnel_company`
- `personnel_department`
- `personnel_position`
- `personnel_area`
- `personnel_employee`
- `personnel_employee_area`
- `iclock_terminal`
- `iclock_terminal_command`
- `iclock_transaction`
- `app_async_job`

### 10.2 Important relations
- Employee -> Company (many-to-one)
- Employee -> Department (many-to-one)
- Employee -> Position (many-to-one)
- Employee -> Superior (self many-to-one)
- Employee <-> Area (many-to-many)
- Terminal -> Area (many-to-one)
- TerminalCommand -> Terminal (many-to-one)
- Transaction -> Terminal, Employee

## 11. How Components Communicate

1. Admin/API creates employees and queues sync actions.
2. Async worker converts each action to one or more `iclock_terminal_command` rows.
3. Device calls `/iclock/getrequest` and receives queued command lines.
4. Device executes command and sends result via `/iclock/devicecmd` or `/iclock/cdata` OPLOG-style payload.
5. Server updates command result (`return_value`, `return_time`).
6. Device uploads attendance via `/iclock/cdata?table=ATTLOG`.
7. Server parses and stores `iclock_transaction` rows.

## 12. Gaps to Reach Full Legacy Parity
- Full parsing of all user/bio upload blocks (`USER`, `FP`, `FACE`, `FVEIN`, `BIODATA`, `BIOPHOTO`, `USERPIC`).
- Full terminal action catalog (`reboot`, `capture`, `daylight saving`, enrollment, delete templates).
- Full command return code mapping and retry policies.
- Full migration of advanced legacy modules and pyd-only behaviors.

## 13. File Paths in Spring Project
- iClock protocol: `zkteco/src/main/java/Zkteco/zkteco/web/IclockController.java`
- iClock management API: `zkteco/src/main/java/Zkteco/zkteco/modules/iclock/controller/IclockManagementController.java`
- iClock service: `zkteco/src/main/java/Zkteco/zkteco/modules/iclock/service/IclockProtocolService.java`
- Employee API: `zkteco/src/main/java/Zkteco/zkteco/modules/employee/controller/EmployeeController.java`
- Employee actions worker: `zkteco/src/main/java/Zkteco/zkteco/modules/employee/service/EmployeeDeviceActionWorker.java`
- Job API: `zkteco/src/main/java/Zkteco/zkteco/modules/job/controller/AsyncJobController.java`
- Migrations: `zkteco/src/main/resources/db/migration/V1__init_employee_iclock.sql`, `V2__async_jobs.sql`

## 14. Version / Build Note
Generated from repository state at current workspace date and compile-verified Spring module (`mvn compile`).
