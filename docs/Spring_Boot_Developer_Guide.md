# ZKTeco Spring Boot Developer Guide

This document explains the `zkteco` Spring Boot application for a new developer.

It focuses on:
- how the project starts
- what each package does
- which routes exist
- how device communication works
- how employee sync works
- how data moves from device to database
- what to read first when you are new

It does not try to explain all of Spring Boot theory. It explains this project.

## 1. What This Project Is

The folder `zkteco` is a Spring Boot application that is gradually replacing parts of the old Python BioTime project.

Main business areas currently implemented:
- `employee` module
  - employee CRUD
  - push employee data to device
  - pull employee data from device
  - remote enroll commands
  - delete biometric or user data from device
- `iclock` module
  - device protocol endpoints
  - device command queue
  - attendance upload and sync
  - manual historical attendance reupload
  - attendance proof / verification
  - device logs and parameter storage
- `job` module
  - async job tracking for device actions

This application talks to ZKTeco devices using the iClock push protocol.

## 2. Tech Stack

From [pom.xml](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/pom.xml):
- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Flyway dependency is present

Current runtime config from [application.properties](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/resources/application.properties):
- default port: `8080`
- default database: `jdbc:postgresql://localhost:5432/zkteco`
- default db user/password: `code` / `code`
- `spring.jpa.hibernate.ddl-auto=update`
- Flyway is currently commented out

Important:
- Because Flyway is disabled right now, schema changes can be created by Hibernate automatically.
- For a production-grade setup, enabling Flyway is cleaner and more predictable.

## 3. How The App Starts

Entry point:
- [ZktecoApplication.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/ZktecoApplication.java)

This is the normal Spring Boot bootstrap class:
- `@SpringBootApplication` tells Spring to scan the package and create beans.
- `main()` starts the embedded server.

What happens at startup:
1. Spring Boot loads configuration from `application.properties`.
2. Spring scans `Zkteco.zkteco` and sub-packages.
3. Controllers, services, repositories, and config classes become beans.
4. JPA connects to PostgreSQL.
5. The app starts listening on port `8080` unless overridden.

## 4. Project Structure

Main source root:
- `zkteco/src/main/java/Zkteco/zkteco`

Important packages:

### 4.1 `config`
- purpose: application-level configuration
- current file:
  - [AsyncConfig.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/config/AsyncConfig.java)

What it does:
- enables `@Async`
- creates `deviceActionExecutor`
- used by async employee/device actions

### 4.2 `domain`
- purpose: JPA entity classes that map Java objects to database tables

Subpackages:
- `domain/common`
  - shared entities like async jobs and audit base class
- `domain/personnel`
  - company, department, area, position, employee
- `domain/iclock`
  - terminal, terminal command, attendance transaction, biometric templates, logs, proof rows

Examples:
- [Employee.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/domain/personnel/Employee.java)
- [Terminal.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/domain/iclock/Terminal.java)
- [AttendanceTransaction.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/domain/iclock/AttendanceTransaction.java)

### 4.3 `repository`
- purpose: database access layer
- each repository usually extends `JpaRepository`
- Spring automatically implements common queries

Examples:
- [EmployeeRepository.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/repository/personnel/EmployeeRepository.java)
- [TerminalRepository.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/repository/iclock/TerminalRepository.java)
- [AttendanceTransactionRepository.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/repository/iclock/AttendanceTransactionRepository.java)

### 4.4 `modules`
- purpose: business logic grouped by feature
- this is the most important package for day-to-day development

Current modules:
- `modules/employee`
- `modules/iclock`
- `modules/job`

Typical structure inside a module:
- `controller`
- `dto`
- `service`

### 4.5 `web`
- purpose: web-facing endpoints and shared web concerns

Current main use:
- device protocol controller:
  - [IclockController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/web/IclockController.java)
- exception handling:
  - [ApiExceptionHandler.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/web/error/ApiExceptionHandler.java)

### 4.6 `model`
- purpose: appears to contain older/generated model classes
- practical advice:
  - treat this package as legacy or secondary unless you know a class is actively used
  - current active business logic mostly uses the `domain` package instead

## 5. The Main Spring Pattern Used Here

Most routes follow this path:

1. Controller receives HTTP request
2. Controller validates and forwards the request to a service
3. Service contains business logic
4. Service calls repositories
5. Repositories read/write database
6. Service returns DTO response
7. Controller returns JSON or plain text

For device protocol routes, the flow is:

1. Device calls `/iclock/...`
2. [IclockController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/web/IclockController.java) receives raw protocol request
3. `IclockProtocolService` parses protocol parameters and body
4. It updates terminal state, saves data, and returns protocol text like `OK` or `C:<id>:<command>`

## 6. Package-By-Package Explanation

## 6.1 `modules/employee`

Main files:
- [EmployeeController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/employee/controller/EmployeeController.java)
- [EmployeeService.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/employee/service/EmployeeService.java)
- [EmployeeDeviceActionService.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/employee/service/EmployeeDeviceActionService.java)
- [EmployeeDeviceActionWorker.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/employee/service/EmployeeDeviceActionWorker.java)

Responsibilities:
- `EmployeeController`
  - defines REST endpoints
- `EmployeeService`
  - handles CRUD for employee records
- `EmployeeDeviceActionService`
  - creates async jobs and dispatches work
- `EmployeeDeviceActionWorker`
  - actually builds terminal commands and stores them in `iclock_terminal_command`

What is important for a new developer:
- Employee device actions are asynchronous.
- The API returns a job record immediately.
- A background worker creates terminal commands.
- The device later polls `/iclock/getrequest` to receive those commands.

## 6.2 `modules/iclock`

This is the heart of device communication.

Main files:
- [IclockController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/web/IclockController.java)
- [IclockProtocolService.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/service/IclockProtocolService.java)
- [IclockAttendanceSyncService.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/service/IclockAttendanceSyncService.java)
- [IclockAttendanceReuploadService.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/service/IclockAttendanceReuploadService.java)
- [IclockAttendanceProofService.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/service/IclockAttendanceProofService.java)
- [IclockManagementController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/controller/IclockManagementController.java)

Responsibilities:
- device protocol endpoints
- terminal registration and heartbeat updates
- command queue management
- command result processing
- attendance upload storage
- user/biometric upload storage
- terminal parameter storage
- operation log and error log storage
- manual historical pull flows

## 6.3 `modules/job`

Main files:
- [AsyncJobController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/job/controller/AsyncJobController.java)
- [AsyncJobService.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/job/service/AsyncJobService.java)

Responsibilities:
- create async job rows
- move job status through:
  - `PENDING`
  - `RUNNING`
  - `SUCCESS`
  - `FAILED`
- allow frontend or developer to query job status

## 7. Database Tables and Their Purpose

These tables are created from migrations in `src/main/resources/db/migration`.

Important migration files:
- [V1__init_employee_iclock.sql](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/resources/db/migration/V1__init_employee_iclock.sql)
- [V2__async_jobs.sql](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/resources/db/migration/V2__async_jobs.sql)
- [V3__iclock_biodata.sql](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/resources/db/migration/V3__iclock_biodata.sql)
- [V4__iclock_terminaluploadlog.sql](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/resources/db/migration/V4__iclock_terminaluploadlog.sql)
- [V5__iclock_logs_and_parameters.sql](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/resources/db/migration/V5__iclock_logs_and_parameters.sql)

Key tables:

### Personnel tables
- `personnel_company`
  - companies
- `personnel_department`
  - departments
- `personnel_position`
  - positions
- `personnel_area`
  - areas / device groups
- `personnel_employee`
  - employees
- `personnel_employee_area`
  - many-to-many relation between employee and area

### iClock tables
- `iclock_terminal`
  - one row per device
- `iclock_terminal_command`
  - queued commands waiting for device poll or holding return values
- `iclock_transaction`
  - attendance punches
- `iclock_biodata`
  - fingerprint / face / palm / vein templates
- `iclock_terminaluploadlog`
  - audit trail for uploads processed by the server
- `iclock_terminallog`
  - operation log rows from device
- `iclock_errorcommandlog`
  - protocol/device error log rows
- `iclock_terminalparameter`
  - saved device info/parameters from `INFO`
- `iclock_transactionproofcmd`
  - attendance verification result rows

### Common/app tables
- `app_async_job`
  - status of async employee/device actions

## 8. Route Reference

This section is the most practical part for a new developer.

## 8.1 Device protocol routes

Handled by [IclockController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/web/IclockController.java).

These routes are not normal JSON APIs. They are protocol endpoints used by devices.

### `GET /iclock/ping`
- purpose: simple health check
- response: `OK`

### `GET /iclock/cdata`
- purpose: some devices send `cdata` as GET
- logic: delegates to `IclockProtocolService.handleCdata`

### `POST /iclock/cdata`
- purpose: main device upload endpoint
- common query params:
  - `SN`
  - `table`
  - `Stamp`
  - `INFO`
  - `DeviceType`
  - `pushver`
- common payload types:
  - `ATTLOG`
  - `OPLOG`
  - `ERRORLOG`
  - `USER`
  - `USERINFO`
  - `FP`
  - `BIODATA`

### `GET /iclock/getrequest`
- purpose: device asks server for the next command
- response if command exists:
  - `C:<commandId>:<commandText>`
- response if no command:
  - `OK`

### `GET /iclock/devicecmd`
- purpose: some devices poll here
- behavior:
  - if result fields are missing, server can fall back to command pull behavior

### `POST /iclock/devicecmd`
- purpose: command result callback
- effect:
  - updates `return_time`
  - updates `return_value`
  - may trigger follow-up logic, for example attendance proof recheck

### `GET /iclock/fdata`
### `POST /iclock/fdata`
- purpose: file/photo/capture related endpoint
- current behavior:
  - tracks upload activity
  - updates capture stamp
  - writes upload audit rows

## 8.2 iClock management API

Handled mainly by [IclockManagementController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/controller/IclockManagementController.java).

These are normal JSON APIs for admins/frontend/tools.

### `GET /api/v1/iclock/devices`
### `GET /api/v1/iclock/terminals`
- purpose: list known terminals

### `GET /api/v1/iclock/terminals/{sn}/commands`
- purpose: list commands for one terminal

### `POST /api/v1/iclock/commands`
- purpose: queue any raw command manually

Example:
```json
{
  "sn": "6073205200002",
  "content": "INFO"
}
```

### `GET /api/v1/iclock/attendance/recent`
- purpose: inspect recent attendance rows
- optional filters:
  - `terminalSn`
  - `empCode`
  - `limit`

### Terminal action routes
- `POST /api/v1/iclock/terminals/{sn}/actions/reboot`
- `POST /api/v1/iclock/terminals/{sn}/actions/read-info`
- `POST /api/v1/iclock/terminals/{sn}/actions/check-all`
- `POST /api/v1/iclock/terminals/{sn}/actions/clear-data`
- `POST /api/v1/iclock/terminals/{sn}/actions/clear-log`
- `POST /api/v1/iclock/terminals/{sn}/actions/set-option`
- `POST /api/v1/iclock/terminals/{sn}/actions/disable`
- `POST /api/v1/iclock/terminals/{sn}/actions/enable`

These endpoints do not directly talk to the device. They queue a command in the database. The device receives the command later when it polls.

## 8.3 Attendance routes

### [IclockAttendanceSyncController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/controller/IclockAttendanceSyncController.java)

#### `POST /api/v1/iclock/attendance/sync`
- purpose: manually inject attendance payload from management side
- useful for testing or backfill

#### `GET /api/v1/iclock/attendance/upload-logs`
- purpose: inspect upload audit rows

### [IclockAttendanceReuploadController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/controller/IclockAttendanceReuploadController.java)

#### `POST /api/v1/iclock/attendance-reupload/commands`
- purpose: manually ask the device to resend old attendance

Behavior:
- `allData=true`
  - attendance device: queues `CHECK LOG`
  - access panel: queues transaction query
- `allData=false`
  - queues `DATA QUERY ATTLOG StartTime=... EndTime=...`

#### `GET /api/v1/iclock/attendance-reupload/commands`
- purpose: list historical pull commands

### [IclockAttendanceProofController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/controller/IclockAttendanceProofController.java)

#### `POST /api/v1/iclock/attendance-proof/commands`
- purpose: ask device for count verification using `VERIFY SUM ATTLOG`

#### `GET /api/v1/iclock/attendance-proof/proofs`
- purpose: list proof results saved in `iclock_transactionproofcmd`

## 8.4 Employee routes

Handled by [EmployeeController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/employee/controller/EmployeeController.java).

### CRUD routes
- `GET /api/v1/employees`
- `GET /api/v1/employees/{id}`
- `POST /api/v1/employees`
- `PUT /api/v1/employees/{id}`
- `DELETE /api/v1/employees/{id}`

### Device action routes
- `POST /api/v1/employees/{id}/actions/sync-to-device`
- `POST /api/v1/employees/{id}/actions/pull-from-device`
- `POST /api/v1/employees/actions/sync-batch-to-device`
- `POST /api/v1/employees/actions/pull-batch-from-device`
- `POST /api/v1/employees/actions/resync-all`
- `POST /api/v1/employees/{id}/actions/remote-enroll`
- `POST /api/v1/employees/{id}/actions/remote-enroll-fingerprint`
- `POST /api/v1/employees/{id}/actions/delete-biometrics`
- `POST /api/v1/employees/{id}/actions/delete-from-device`

Important:
- all device action routes return an async job
- the actual device command is created later by the worker

## 8.5 Async job route

Handled by [AsyncJobController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/job/controller/AsyncJobController.java).

### `GET /api/v1/jobs/{id}`
- purpose: check job status after an async employee/device action

## 9. Important Request Flows

## 9.1 Flow: Device connects for the first time

1. Device calls `/iclock/cdata?SN=...`
2. `IclockProtocolService.touchTerminal()` looks up terminal by serial number
3. If terminal does not exist, a new terminal row is created
4. Last activity, IP, and other metadata are updated
5. Terminal is saved in `iclock_terminal`

This is why the system can auto-register a new device.

## 9.2 Flow: Device uploads attendance

1. Device calls `POST /iclock/cdata?SN=...&table=ATTLOG`
2. `IclockController` forwards request to `IclockProtocolService`
3. `IclockProtocolService` updates terminal stamp
4. It delegates parsing to `IclockAttendanceSyncService`
5. `IclockAttendanceSyncService` parses each attendance line
6. Each valid punch becomes one `AttendanceTransaction`
7. Duplicates are skipped
8. Upload audit is saved to `iclock_terminaluploadlog`

Main classes involved:
- [IclockController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/web/IclockController.java)
- [IclockProtocolService.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/service/IclockProtocolService.java)
- [IclockAttendanceSyncService.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/service/IclockAttendanceSyncService.java)

## 9.3 Flow: Server sends a command to device

1. Admin or API calls an action endpoint like `read-info`, `reboot`, or employee sync
2. The service creates a row in `iclock_terminal_command`
3. Device later polls `GET /iclock/getrequest?SN=...`
4. Server returns `C:<id>:<command>`
5. Device executes it
6. Device reports result through `/iclock/devicecmd` or another protocol path
7. `return_time` and `return_value` are updated

This is a queue-based design. The server does not open a socket to the device.

## 9.4 Flow: Manual historical attendance pull

1. Call `POST /api/v1/iclock/attendance-reupload/commands`
2. Service builds:
   - `CHECK LOG`, or
   - `DATA QUERY ATTLOG StartTime=... EndTime=...`
3. Command is saved in `iclock_terminal_command`
4. Device polls `/iclock/getrequest`
5. Device receives command
6. Device sends old logs back to `/iclock/cdata`
7. Server stores them in `iclock_transaction`

## 9.5 Flow: Attendance proof / correction

1. Call `POST /api/v1/iclock/attendance-proof/commands`
2. Server queues `VERIFY SUM ATTLOG StartTime=... EndTime=...`
3. Device returns a result with log count
4. `IclockProtocolService.saveCommandResult()` processes the result
5. Server counts rows already stored in `iclock_transaction`
6. Result is saved to `iclock_transactionproofcmd`
7. If server count is smaller than device count, the server automatically queues:
   - `DATA QUERY ATTLOG StartTime=... EndTime=...`

This is the automatic recheck logic.

## 9.6 Flow: Employee sync to device

1. Call `POST /api/v1/employees/{id}/actions/sync-to-device`
2. `EmployeeDeviceActionService` creates async job
3. `EmployeeDeviceActionWorker` runs in background
4. Worker resolves target terminals
5. Worker builds:
   - user info command
   - biometric template commands if present
6. Commands are saved in `iclock_terminal_command`
7. Device polls `/iclock/getrequest`
8. Device executes received commands

## 9.7 Flow: Pull employee from device

1. Call `POST /api/v1/employees/{id}/actions/pull-from-device`
2. Async worker queues:
   - `DATA QUERY USERINFO PIN=<empCode>`
3. Device later returns user rows through `/iclock/cdata`
4. `IclockProtocolService` updates or creates employee rows

## 9.8 Flow: Delete employee from device

1. Call `POST /api/v1/employees/{id}/actions/delete-from-device`
2. Worker decides the correct delete command for that device type:
   - `DATA DELETE USERINFO PIN=...`
   - or `DATA DEL_USER PIN=...`
   - or access-control variant
3. Worker also queues biometric delete commands when requested
4. Device gets commands on next poll

## 10. What Each Important Service Does

## 10.1 `EmployeeService`
- normal CRUD service
- validates employee code uniqueness
- resolves company, department, position, areas
- maps entity to API DTO

Read this when you want to:
- add employee fields
- change employee validation
- change employee response JSON

## 10.2 `EmployeeDeviceActionWorker`
- builds actual device commands
- this is the place to change employee-device command behavior

Read this when you want to:
- change sync command format
- add new biometric command types
- add more device actions

## 10.3 `IclockProtocolService`
- central protocol engine
- one of the most important files in the whole project

It handles:
- terminal registration
- terminal metadata updates
- `cdata`
- `getrequest`
- `devicecmd`
- `fdata`
- command result side effects
- mixed uploads
- registration uploads
- log storage

Read this first if:
- device is online but data is not stored
- device gets commands but result is not processed
- stamps or protocol formats are wrong

## 10.4 `IclockAttendanceSyncService`
- focused service for attendance parsing and saving
- keeps attendance logic out of the larger protocol service

Read this when:
- attendance lines are malformed
- duplicate detection is wrong
- one device format needs parser changes

## 10.5 `IclockAttendanceReuploadService`
- manual old-attendance pull command builder

Read this when:
- user wants a button to resend old logs
- you need different historical range logic

## 10.6 `IclockAttendanceProofService`
- queues `VERIFY SUM ATTLOG`
- lists proof records

Read this when:
- you want reconciliation tools
- you want to inspect count mismatches

## 10.7 `AsyncJobService`
- tracks background job status
- not device protocol logic itself
- just operational visibility for async actions

## 11. DTOs: Why They Exist

DTO means Data Transfer Object.

In this project DTOs are used for:
- request body input
- API response output
- keeping controller JSON format separate from entity classes

Examples:
- `EmployeeUpsertRequest`
- `EmployeeResponse`
- `TerminalCommandCreateRequest`
- `AttendanceReuploadRequest`
- `AttendanceProofResponse`

Practical rule:
- entity = database structure
- DTO = API structure

Do not expose entity classes directly from controllers unless there is a strong reason.

## 12. Error Handling

Handled by:
- [ApiExceptionHandler.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/web/error/ApiExceptionHandler.java)

Main exception types:
- `BadRequestException`
- `NotFoundException`

What it does:
- converts exceptions to JSON with:
  - timestamp
  - status
  - error
  - message

Important note:
- device protocol routes return plain text, not JSON
- normal management APIs return JSON

## 13. Async Processing

Configured in:
- [AsyncConfig.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/config/AsyncConfig.java)

Executor:
- bean name: `deviceActionExecutor`

Used by:
- methods annotated with `@Async("deviceActionExecutor")`

Why async is used:
- employee/device actions can create many commands
- API should return quickly
- status is tracked in `app_async_job`

## 14. How To Run The Project

From `zkteco` folder:

```bash
./mvnw spring-boot:run
```

Or build first:

```bash
./mvnw clean package
java -jar target/zkteco-0.0.1-SNAPSHOT.jar
```

Common environment variables:

```bash
export SERVER_PORT=8080
export DB_URL=jdbc:postgresql://localhost:5432/zkteco
export DB_USER=code
export DB_PASSWORD=code
```

Compile only:

```bash
./mvnw -DskipTests compile
```

## 15. What To Read First As A New Developer

Recommended order:

1. [ZktecoApplication.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/ZktecoApplication.java)
2. [application.properties](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/resources/application.properties)
3. [IclockController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/web/IclockController.java)
4. [IclockProtocolService.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/service/IclockProtocolService.java)
5. [IclockAttendanceSyncService.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/service/IclockAttendanceSyncService.java)
6. [EmployeeController.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/employee/controller/EmployeeController.java)
7. [EmployeeService.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/employee/service/EmployeeService.java)
8. [EmployeeDeviceActionWorker.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/employee/service/EmployeeDeviceActionWorker.java)
9. migrations in `src/main/resources/db/migration`

If you only want to understand device connectivity, start with:
- `IclockController`
- `IclockProtocolService`
- `IclockAttendanceSyncService`

If you only want to understand employee device actions, start with:
- `EmployeeController`
- `EmployeeDeviceActionService`
- `EmployeeDeviceActionWorker`

## 16. How To Add A New Feature In This Project

Simple pattern:

1. Add or update entity if database structure changes
2. Add or update repository query methods
3. Add request/response DTOs
4. Add service logic
5. Add controller endpoint
6. If async work is needed, create job + worker method
7. Compile and test

Example cases:

### Add a normal JSON API
Use:
- `modules/<feature>/dto`
- `modules/<feature>/service`
- `modules/<feature>/controller`

### Add a new device protocol behavior
Usually update:
- [IclockProtocolService.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/service/IclockProtocolService.java)

Possibly also:
- `IclockAttendanceSyncService`
- repository methods
- migration for new table

### Add a new employee-to-device action
Usually update:
- `EmployeeController`
- `EmployeeDeviceActionService`
- `EmployeeDeviceActionWorker`

## 17. Important Project-Specific Rules

These are practical rules for this codebase:

### 17.1 Device routes return plain text
Do not accidentally convert `/iclock/*` routes into JSON APIs.

Devices expect responses like:
- `OK`
- `OK:1`
- `C:12:CHECK LOG`

### 17.2 The server queues commands, it does not push directly
The server waits for the device to poll.

### 17.3 Time is stored in UTC
The project sets:
- `spring.jpa.properties.hibernate.jdbc.time_zone=UTC`

Be careful with local time vs device time vs DB time.

### 17.4 A terminal may be auto-created
If a device connects with a new `SN`, the protocol service can create it automatically.

### 17.5 The `model` package is not the main active layer
Prefer `domain` + `repository` + `modules` unless you confirm otherwise.

## 18. Known Caveats

Current caveats a new developer should know:

- Flyway is present but disabled in config.
- Some behavior is still being migrated from the Python project.
- The device protocol has many variants depending on firmware and device type.
- Some `fdata` behavior is currently tracked/audited rather than fully persisted as files.
- Access-control devices and attendance devices do not always use the same commands.

## 19. Quick Mental Model

If you want one short mental model for this project, use this:

- `employee` = business data and employee actions
- `iclock` = device protocol and device-side synchronization
- `job` = async operation tracking
- `domain` = table mappings
- `repository` = database access
- `controller` = HTTP entry point
- `service` = real business logic

The most important file in the project is:
- [IclockProtocolService.java](/media/waheed/Project1/Project/att/zktecobio/mysite/zkteco/src/main/java/Zkteco/zkteco/modules/iclock/service/IclockProtocolService.java)

That file is the bridge between:
- device requests
- command results
- employee/device sync
- data stored in PostgreSQL

## 20. Suggested Next Reading

After reading this guide:

1. Trigger one simple command like `read-info`
2. Inspect `iclock_terminal_command`
3. Make the device poll `/iclock/getrequest`
4. Watch how the command gets `transfer_time`
5. Upload one attendance row to `/iclock/cdata`
6. Inspect `iclock_transaction`

That sequence makes the architecture much easier to understand.
