package Zkteco.zkteco.modules.iclock.service;

import Zkteco.zkteco.domain.iclock.AttendanceTransaction;
import Zkteco.zkteco.domain.iclock.Terminal;
import Zkteco.zkteco.domain.iclock.TerminalUploadLog;
import Zkteco.zkteco.domain.personnel.Area;
import Zkteco.zkteco.domain.personnel.Company;
import Zkteco.zkteco.domain.personnel.Department;
import Zkteco.zkteco.domain.personnel.Employee;
import Zkteco.zkteco.modules.iclock.dto.AttendanceSyncRequest;
import Zkteco.zkteco.modules.iclock.dto.AttendanceSyncResponse;
import Zkteco.zkteco.modules.iclock.dto.TerminalUploadLogResponse;
import Zkteco.zkteco.repository.iclock.AttendanceTransactionRepository;
import Zkteco.zkteco.repository.iclock.TerminalRepository;
import Zkteco.zkteco.repository.iclock.TerminalUploadLogRepository;
import Zkteco.zkteco.repository.personnel.AreaRepository;
import Zkteco.zkteco.repository.personnel.CompanyRepository;
import Zkteco.zkteco.repository.personnel.DepartmentRepository;
import Zkteco.zkteco.repository.personnel.EmployeeRepository;
import Zkteco.zkteco.web.error.BadRequestException;
import Zkteco.zkteco.web.error.NotFoundException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IclockAttendanceSyncService {

    private static final DateTimeFormatter PUNCH_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TerminalRepository terminalRepository;
    private final AttendanceTransactionRepository attendanceTransactionRepository;
    private final TerminalUploadLogRepository terminalUploadLogRepository;
    private final EmployeeRepository employeeRepository;
    private final AreaRepository areaRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;

    public IclockAttendanceSyncService(
            TerminalRepository terminalRepository,
            AttendanceTransactionRepository attendanceTransactionRepository,
            TerminalUploadLogRepository terminalUploadLogRepository,
            EmployeeRepository employeeRepository,
            AreaRepository areaRepository,
            DepartmentRepository departmentRepository,
            CompanyRepository companyRepository
    ) {
        this.terminalRepository = terminalRepository;
        this.attendanceTransactionRepository = attendanceTransactionRepository;
        this.terminalUploadLogRepository = terminalUploadLogRepository;
        this.employeeRepository = employeeRepository;
        this.areaRepository = areaRepository;
        this.departmentRepository = departmentRepository;
        this.companyRepository = companyRepository;
    }

    public AttendanceSyncResponse syncFromManagement(AttendanceSyncRequest request) {
        String payload = buildPayload(request.getPayload(), request.getLines());
        if (payload == null || payload.isBlank()) {
            throw new BadRequestException("Attendance payload is required");
        }

        Terminal terminal = terminalRepository.findBySn(request.getSn().trim())
                .orElseThrow(() -> new NotFoundException("Terminal not found: " + request.getSn()));

        String event = normalizeEvent(request.getEvent(), "MANUAL-ATTLOG");
        return syncAttendance(terminal, payload, event, "manual-sync");
    }

    public AttendanceSyncResponse syncFromDevice(Terminal terminal, String body) {
        return syncAttendance(terminal, body, "ATTLOG", "device-sync");
    }

    @Transactional(readOnly = true)
    public List<TerminalUploadLogResponse> listUploadLogs(String terminalSn, Integer limit) {
        int max = limit == null ? 50 : Math.max(1, Math.min(limit, 200));
        List<TerminalUploadLog> logs = terminalSn == null || terminalSn.isBlank()
                ? terminalUploadLogRepository.findTop200ByOrderByUploadTimeDescIdDesc()
                : terminalUploadLogRepository.findTop200ByTerminal_SnOrderByUploadTimeDescIdDesc(terminalSn.trim());

        return logs.stream()
                .limit(max)
                .map(this::mapUploadLog)
                .toList();
    }

    private AttendanceSyncResponse syncAttendance(Terminal terminal, String body, String event, String contentTag) {
        OffsetDateTime now = OffsetDateTime.now();
        if (body == null || body.isBlank()) {
            terminal.setUploadTime(now);
            terminalRepository.save(terminal);
            saveUploadLog(terminal, event, contentTag + ":empty", 0, 0, now);
            return new AttendanceSyncResponse(
                    terminal.getId(),
                    terminal.getSn(),
                    terminal.getAlias(),
                    0,
                    0,
                    0,
                    0,
                    now
            );
        }

        int received = 0;
        int saved = 0;
        int duplicate = 0;
        int error = 0;

        for (String rawLine : splitLines(body)) {
            String line = normalizeAttendanceLine(rawLine);
            if (line == null) {
                continue;
            }

            received++;
            Optional<AttendanceTransaction> txOpt = parseAttendance(terminal, line, now);
            if (txOpt.isEmpty()) {
                error++;
                continue;
            }

            AttendanceTransaction tx = txOpt.get();
            try {
                if (isDuplicate(tx)) {
                    duplicate++;
                    continue;
                }
                attendanceTransactionRepository.save(tx);
                saved++;
            } catch (DataIntegrityViolationException ignored) {
                duplicate++;
            }
        }

        terminal.setUploadTime(now);
        terminalRepository.save(terminal);
        saveUploadLog(terminal, event, summarizeContent(contentTag, saved, duplicate, error), received, error, now);

        return new AttendanceSyncResponse(
                terminal.getId(),
                terminal.getSn(),
                terminal.getAlias(),
                received,
                saved,
                duplicate,
                error,
                now
        );
    }

    private void saveUploadLog(
            Terminal terminal,
            String event,
            String content,
            int uploadCount,
            int errorCount,
            OffsetDateTime uploadTime
    ) {
        TerminalUploadLog uploadLog = new TerminalUploadLog();
        uploadLog.setTerminal(terminal);
        uploadLog.setEvent(truncate(event, 80));
        uploadLog.setContent(truncate(content, 80));
        uploadLog.setUploadCount(uploadCount);
        uploadLog.setErrorCount(errorCount);
        uploadLog.setUploadTime(uploadTime);
        terminalUploadLogRepository.save(uploadLog);
    }

    private boolean isDuplicate(AttendanceTransaction tx) {
        if (tx.getCompanyCode() != null && !tx.getCompanyCode().isBlank()) {
            return attendanceTransactionRepository.existsByCompanyCodeAndEmpCodeAndPunchTime(
                    tx.getCompanyCode(),
                    tx.getEmpCode(),
                    tx.getPunchTime()
            );
        }
        return attendanceTransactionRepository.existsByTerminalSnAndEmpCodeAndPunchTime(
                tx.getTerminalSn(),
                tx.getEmpCode(),
                tx.getPunchTime()
        );
    }

    private Optional<AttendanceTransaction> parseAttendance(Terminal terminal, String line, OffsetDateTime uploadTime) {
        if (line.contains("=")) {
            return parseAttendanceFields(terminal, parseKvPayload(line), uploadTime);
        }
        return parseAttendanceColumns(terminal, line, uploadTime);
    }

    private Optional<AttendanceTransaction> parseAttendanceColumns(Terminal terminal, String line, OffsetDateTime uploadTime) {
        String[] cols = line.split("\\t");
        if (cols.length < 2) {
            return Optional.empty();
        }

        String pin = cols[0].trim();
        if (pin.isEmpty()) {
            return Optional.empty();
        }

        ParsedPunchTime parsedTime = parsePunchTime(cols[1].trim(), valueOrDefault(cols, 7, null), valueOrDefault(cols, 8, null));
        if (parsedTime == null) {
            return Optional.empty();
        }

        return Optional.of(buildAttendanceTransaction(
                terminal,
                pin,
                parsedTime,
                valueOrDefault(cols, 2, "255"),
                parseInt(valueOrDefault(cols, 3, "0"), 0),
                blankToNull(valueOrDefault(cols, 4, null)),
                blankToNull(valueOrDefault(cols, 5, null)),
                uploadTime
        ));
    }

    private Optional<AttendanceTransaction> parseAttendanceFields(Terminal terminal, Map<String, String> fields, OffsetDateTime uploadTime) {
        String pin = firstNonBlank(fields.get("PIN"), fields.get("Pin"), fields.get("emp_code"));
        String punchTimeValue = firstNonBlank(
                fields.get("DateTime"),
                fields.get("PUNCHTIME"),
                fields.get("PunchTime"),
                fields.get("Time")
        );
        if (pin == null || punchTimeValue == null) {
            return Optional.empty();
        }

        ParsedPunchTime parsedTime = parsePunchTime(
                punchTimeValue,
                firstNonBlank(fields.get("Longitude"), fields.get("longitude"), fields.get("Lon")),
                firstNonBlank(fields.get("Latitude"), fields.get("latitude"), fields.get("Lat"))
        );
        if (parsedTime == null) {
            return Optional.empty();
        }

        AttendanceTransaction tx = buildAttendanceTransaction(
                terminal,
                pin,
                parsedTime,
                firstNonBlank(fields.get("Status"), fields.get("PunchState"), fields.get("PUNCHSTATE"), "255"),
                parseInt(firstNonBlank(fields.get("Verify"), fields.get("VERIFY"), "0"), 0),
                blankToNull(firstNonBlank(fields.get("WorkCode"), fields.get("WORKCODE"))),
                blankToNull(firstNonBlank(fields.get("Reserved"), fields.get("RESERVED"))),
                uploadTime
        );

        tx.setGpsLocation(blankToNull(firstNonBlank(fields.get("GPSLocation"), fields.get("Location"))));
        tx.setMobile(blankToNull(firstNonBlank(fields.get("Mobile"), fields.get("mobile"))));
        tx.setIsMask((short) parseInt(firstNonBlank(fields.get("MaskFlag"), fields.get("MASKFLAG")), 255).intValue());
        tx.setTemperature(parseDecimal(firstNonBlank(fields.get("Temperature"), fields.get("TEMP")), new BigDecimal("255.0")));
        return Optional.of(tx);
    }

    private AttendanceTransaction buildAttendanceTransaction(
            Terminal terminal,
            String pin,
            ParsedPunchTime parsedTime,
            String punchState,
            Integer verifyType,
            String workCode,
            String reserved,
            OffsetDateTime uploadTime
    ) {
        Employee employee = resolveEmployee(terminal, pin);
        String companyCode = resolveCompanyCode(terminal, employee);

        AttendanceTransaction tx = new AttendanceTransaction();
        tx.setCompanyCode(companyCode);
        tx.setEmpCode(pin.trim());
        tx.setEmployee(employee);
        tx.setPunchTime(parsedTime.punchTime());
        tx.setPunchState(blankToNull(punchState) != null ? punchState.trim() : "255");
        tx.setVerifyType(verifyType != null ? verifyType : 0);
        tx.setWorkCode(workCode);
        tx.setReserved(reserved);
        tx.setTerminal(terminal);
        tx.setTerminalSn(terminal.getSn());
        tx.setTerminalAlias(terminal.getAlias());
        tx.setAreaAlias(terminal.getArea() != null ? terminal.getArea().getAreaName() : null);
        tx.setLongitude(parsedTime.longitude());
        tx.setLatitude(parsedTime.latitude());
        tx.setSource((short) 1);
        tx.setPurpose(terminal.getProductType() != null ? terminal.getProductType() : (short) 9);
        tx.setCrc(crcFor(parsedTime.punchTime()));
        tx.setIsAttendance(terminal.getIsAttendance() != null ? terminal.getIsAttendance() : (short) 1);
        tx.setUploadTime(uploadTime);
        tx.setSyncStatus((short) 0);
        tx.setIsMask((short) 255);
        tx.setTemperature(new BigDecimal("255.0"));
        return tx;
    }

    private Employee resolveEmployee(Terminal terminal, String pin) {
        if (terminal.getArea() != null && terminal.getArea().getCompany() != null) {
            Long companyId = terminal.getArea().getCompany().getId();
            Optional<Employee> sameCompany = employeeRepository.findByCompanyIdAndEmpCode(companyId, pin);
            if (sameCompany.isPresent()) {
                return sameCompany.get();
            }
        }
        return employeeRepository.findFirstByEmpCodeOrderByIdAsc(pin).orElse(null);
    }

    private String resolveCompanyCode(Terminal terminal, Employee employee) {
        if (employee != null && employee.getCompany() != null && employee.getCompany().getCompanyCode() != null) {
            return employee.getCompany().getCompanyCode();
        }
        if (terminal.getArea() != null && terminal.getArea().getCompany() != null
                && terminal.getArea().getCompany().getCompanyCode() != null) {
            return terminal.getArea().getCompany().getCompanyCode();
        }
        return "";
    }

    private Terminal buildAutoTerminal(String sn) {
        Terminal terminal = new Terminal();
        terminal.setSn(sn);
        terminal.setAlias("Auto add");
        terminal.setProductType((short) 9);
        terminal.setIsAttendance((short) 1);
        terminal.setIsRegistration((short) 0);
        terminal.setControllerType((short) 0);
        terminal.setAuthentication((short) 1);
        terminal.setUploadFlag("1111100000");
        terminal.setTransferTime("00:00;14:05");
        terminal.setHeartbeat(10);
        terminal.setTransferMode((short) 1);
        terminal.setTransferInterval(1);
        terminal.setState(1);

        Area defaultArea = areaRepository.findFirstByDefaultAreaTrueOrderByIdAsc()
                .orElseGet(() -> areaRepository.findById(1L).orElse(null));
        terminal.setArea(defaultArea);
        return terminal;
    }

    private Employee createEmployeeForDeviceUpload(Terminal terminal, String pin) {
        Company company = terminal.getArea() != null ? terminal.getArea().getCompany() : null;
        if (company == null) {
            company = companyRepository.findById(1L).orElse(null);
        }
        if (company == null) {
            return null;
        }

        Department department = departmentRepository.findFirstByCompanyIdOrderByIdAsc(company.getId())
                .or(() -> departmentRepository.findFirstByOrderByIdAsc())
                .orElse(null);
        if (department == null) {
            return null;
        }

        Employee employee = new Employee();
        employee.setCompany(company);
        employee.setDepartment(department);
        employee.setEmpCode(pin.trim());
        employee.setFirstName(pin.trim());
        employee.setDevPrivilege(0);
        employee.setVerifyMode(0);
        employee.setUpdateTime(OffsetDateTime.now());
        employee.setEnrollSn(terminal.getSn());
        if (terminal.getArea() != null) {
            employee.getAreas().add(terminal.getArea());
        }
        return employeeRepository.save(employee);
    }

    private String buildPayload(String payload, List<String> lines) {
        if (payload != null && !payload.isBlank()) {
            return payload;
        }
        if (lines == null || lines.isEmpty()) {
            return payload;
        }

        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append(line);
        }
        return builder.toString();
    }

    private String normalizeAttendanceLine(String rawLine) {
        if (rawLine == null) {
            return null;
        }
        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith("ID=") || line.startsWith("OPLOG")) {
            return null;
        }
        if (line.regionMatches(true, 0, "ATTLOG ", 0, 7)) {
            line = line.substring(7).trim();
        }
        return line.isEmpty() ? null : line;
    }

    private ParsedPunchTime parsePunchTime(String value, String lonValue, String latValue) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String rawTime = value.trim();
        Double longitude = parseDouble(lonValue, null);
        Double latitude = parseDouble(latValue, null);

        if (rawTime.contains(",")) {
            String[] parts = rawTime.split(",");
            rawTime = parts[0].trim();
            if (parts.length > 2) {
                longitude = parseDouble(parts[1], longitude);
                latitude = parseDouble(parts[2], latitude);
            }
        }

        try {
            LocalDateTime parsed = LocalDateTime.parse(rawTime, PUNCH_TIME_FORMAT);
            return new ParsedPunchTime(parsed.atOffset(ZoneOffset.UTC), longitude, latitude);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private Map<String, String> parseKvPayload(String payload) {
        Map<String, String> out = new LinkedHashMap<>();
        if (payload == null || payload.isBlank()) {
            return out;
        }

        String normalized = payload.replace("\r", "\n");
        String[] items = normalized.split("[\\n\\t&]");
        for (String item : items) {
            String token = item.trim();
            if (token.isEmpty()) {
                continue;
            }
            int idx = token.indexOf('=');
            if (idx <= 0 || idx == token.length() - 1) {
                continue;
            }
            String key = token.substring(0, idx).trim();
            String value = token.substring(idx + 1).trim();
            if (!key.isEmpty()) {
                out.put(key, value);
            }
        }
        return out;
    }

    private List<String> splitLines(String body) {
        String normalized = body.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\\n");
        List<String> out = new ArrayList<>(lines.length);
        for (String line : lines) {
            if (!line.isBlank()) {
                out.add(line);
            }
        }
        return out;
    }

    private String summarizeContent(String contentTag, int saved, int duplicate, int error) {
        return truncate(contentTag + ":ok=" + saved + ",dup=" + duplicate + ",err=" + error, 80);
    }

    private String normalizeEvent(String event, String defaultValue) {
        if (event == null || event.isBlank()) {
            return defaultValue;
        }
        return truncate(event.trim().toUpperCase(Locale.ROOT), 80);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String valueOrDefault(String[] array, int index, String defaultValue) {
        if (index >= array.length) {
            return defaultValue;
        }
        return array[index];
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer parseInt(String value, Integer defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private Double parseDouble(String value, Double defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private BigDecimal parseDecimal(String value, BigDecimal defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String crcFor(OffsetDateTime punchTime) {
        String source = punchTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return source;
        }
    }

    private TerminalUploadLogResponse mapUploadLog(TerminalUploadLog uploadLog) {
        return new TerminalUploadLogResponse(
                uploadLog.getId(),
                uploadLog.getTerminal().getId(),
                uploadLog.getTerminal().getSn(),
                uploadLog.getEvent(),
                uploadLog.getContent(),
                uploadLog.getUploadCount(),
                uploadLog.getErrorCount(),
                uploadLog.getUploadTime()
        );
    }

    private record ParsedPunchTime(OffsetDateTime punchTime, Double longitude, Double latitude) {
    }
}
