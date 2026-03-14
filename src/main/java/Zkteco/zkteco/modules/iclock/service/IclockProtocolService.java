package Zkteco.zkteco.modules.iclock.service;

import Zkteco.zkteco.domain.iclock.AttendanceTransaction;
import Zkteco.zkteco.domain.iclock.BioDataTemplate;
import Zkteco.zkteco.domain.iclock.ErrorCommandLog;
import Zkteco.zkteco.domain.iclock.Terminal;
import Zkteco.zkteco.domain.iclock.TerminalCommand;
import Zkteco.zkteco.domain.iclock.TerminalLog;
import Zkteco.zkteco.domain.iclock.TerminalParameter;
import Zkteco.zkteco.domain.iclock.TerminalUploadLog;
import Zkteco.zkteco.domain.iclock.TransactionProofCommand;
import Zkteco.zkteco.domain.personnel.Area;
import Zkteco.zkteco.domain.personnel.Company;
import Zkteco.zkteco.domain.personnel.Department;
import Zkteco.zkteco.domain.personnel.Employee;
import Zkteco.zkteco.repository.iclock.AttendanceTransactionRepository;
import Zkteco.zkteco.repository.iclock.BioDataTemplateRepository;
import Zkteco.zkteco.repository.iclock.ErrorCommandLogRepository;
import Zkteco.zkteco.repository.iclock.TerminalCommandRepository;
import Zkteco.zkteco.repository.iclock.TerminalLogRepository;
import Zkteco.zkteco.repository.iclock.TerminalParameterRepository;
import Zkteco.zkteco.repository.iclock.TerminalRepository;
import Zkteco.zkteco.repository.iclock.TerminalUploadLogRepository;
import Zkteco.zkteco.repository.iclock.TransactionProofCommandRepository;
import Zkteco.zkteco.repository.personnel.AreaRepository;
import Zkteco.zkteco.repository.personnel.CompanyRepository;
import Zkteco.zkteco.repository.personnel.DepartmentRepository;
import Zkteco.zkteco.repository.personnel.EmployeeRepository;
import Zkteco.zkteco.modules.iclock.dto.TerminalCommandResponse;
import Zkteco.zkteco.modules.iclock.dto.AttendanceTransactionResponse;
import Zkteco.zkteco.modules.iclock.dto.AttendanceSyncResponse;
import Zkteco.zkteco.modules.iclock.dto.TerminalResponse;
import Zkteco.zkteco.web.error.BadRequestException;
import Zkteco.zkteco.web.error.NotFoundException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
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
public class IclockProtocolService {

    private static final DateTimeFormatter PUNCH_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TerminalRepository terminalRepository;
    private final TerminalCommandRepository terminalCommandRepository;
    private final AttendanceTransactionRepository attendanceTransactionRepository;
    private final BioDataTemplateRepository bioDataTemplateRepository;
    private final TerminalLogRepository terminalLogRepository;
    private final ErrorCommandLogRepository errorCommandLogRepository;
    private final TerminalParameterRepository terminalParameterRepository;
    private final TerminalUploadLogRepository terminalUploadLogRepository;
    private final TransactionProofCommandRepository transactionProofCommandRepository;
    private final EmployeeRepository employeeRepository;
    private final AreaRepository areaRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final IclockAttendanceSyncService attendanceSyncService;

    public IclockProtocolService(
            TerminalRepository terminalRepository,
            TerminalCommandRepository terminalCommandRepository,
            AttendanceTransactionRepository attendanceTransactionRepository,
            BioDataTemplateRepository bioDataTemplateRepository,
            TerminalLogRepository terminalLogRepository,
            ErrorCommandLogRepository errorCommandLogRepository,
            TerminalParameterRepository terminalParameterRepository,
            TerminalUploadLogRepository terminalUploadLogRepository,
            TransactionProofCommandRepository transactionProofCommandRepository,
            EmployeeRepository employeeRepository,
            AreaRepository areaRepository,
            DepartmentRepository departmentRepository,
            CompanyRepository companyRepository,
            IclockAttendanceSyncService attendanceSyncService
    ) {
        this.terminalRepository = terminalRepository;
        this.terminalCommandRepository = terminalCommandRepository;
        this.attendanceTransactionRepository = attendanceTransactionRepository;
        this.bioDataTemplateRepository = bioDataTemplateRepository;
        this.terminalLogRepository = terminalLogRepository;
        this.errorCommandLogRepository = errorCommandLogRepository;
        this.terminalParameterRepository = terminalParameterRepository;
        this.terminalUploadLogRepository = terminalUploadLogRepository;
        this.transactionProofCommandRepository = transactionProofCommandRepository;
        this.employeeRepository = employeeRepository;
        this.areaRepository = areaRepository;
        this.departmentRepository = departmentRepository;
        this.companyRepository = companyRepository;
        this.attendanceSyncService = attendanceSyncService;
    }

    public String handleCdata(Map<String, String> params, String body, String remoteIp) {
        String sn = required(params, "SN");
        Terminal terminal = touchTerminal(sn, params, remoteIp);
        String infoData = params.get("INFO");
        if (infoData != null && !infoData.isBlank()) {
            Map<String, String> infoFields = parseKvPayload(infoData);
            applyDeviceInfo(terminal, infoFields);
            terminalRepository.save(terminal);
            persistTerminalParameters(terminal, infoFields);
        }

        String table = normalizeUpper(params.get("table"));
        if ("ATTLOG".equals(table)) {
            updateLogStamp(terminal, firstNonBlank(params.get("Stamp"), params.get("stamp")));
            AttendanceSyncResponse sync = attendanceSyncService.syncFromDevice(terminal, body);
            return "OK:" + sync.savedCount();
        }

        if (isRegistrationTable(table)) {
            int saved = ingestRegistrationTable(terminal, table, body);
            return "OK:" + saved;
        }

        if ("OPLOG".equals(table) || "OPERLOG".equals(table)) {
            updateOpLogStamp(terminal, firstNonBlank(params.get("OpStamp"), params.get("OPStamp"), params.get("Stamp")));
            int saved = ingestOperationLogs(terminal, body, params);
            int updated = saveCommandResult(terminal, body, params);
            saveProtocolUploadLog(terminal, "OPLOG", "operation-log", saved, 0);
            return "OK:" + (saved + updated);
        }

        if ("ERRORLOG".equals(table)) {
            int saved = ingestErrorLogs(terminal, body, params);
            saveProtocolUploadLog(terminal, "ERRORLOG", "error-log", saved, 0);
            return "OK:" + saved;
        }

        if (isCommandResultPayload(body)) {
            int updated = saveCommandResult(terminal, body, params);
            return "OK:" + updated;
        }

        MixedUploadResult mixed = ingestMixedBody(terminal, body, params);
        if (mixed.totalProcessed() > 0) {
            return "OK:" + mixed.totalProcessed();
        }

        int prefixedSaved = ingestPrefixedRegistrationLines(terminal, body);
        if (prefixedSaved > 0) {
            return "OK:" + prefixedSaved;
        }

        return "OK";
    }

    public String handleFdata(Map<String, String> params, String body, String remoteIp) {
        String sn = required(params, "SN");
        Terminal terminal = touchTerminal(sn, params, remoteIp);
        updateCaptureStamp(terminal, firstNonBlank(params.get("Stamp"), params.get("CaptureStamp"), params.get("capture_stamp")));

        int uploads = 0;
        if (body != null && !body.isBlank()) {
            String bodyUpper = body.toUpperCase(Locale.ROOT);
            if (bodyUpper.contains("CMD=UPLOADPHOTO") || bodyUpper.contains("CMD=REALUPLOAD")) {
                uploads = 1;
                saveProtocolUploadLog(terminal, "FDATA", "capture-upload", 1, 0);
            } else if (bodyUpper.contains("BIOPHOTO") || bodyUpper.contains("USERPIC")) {
                uploads = 1;
                saveProtocolUploadLog(terminal, "FDATA", "photo-upload", 1, 0);
            }
        }

        if (uploads == 0) {
            saveProtocolUploadLog(terminal, "FDATA", "generic-upload", 1, 0);
            uploads = 1;
        }

        terminal.setUploadTime(OffsetDateTime.now());
        terminalRepository.save(terminal);
        return "OK:" + uploads;
    }

    public String handleGetRequest(Map<String, String> params, String remoteIp) {
        String sn = required(params, "SN");
        Terminal terminal = touchTerminal(sn, params, remoteIp);

        Optional<TerminalCommand> pendingOpt = terminalCommandRepository
                .findTopByTerminalIdAndTransferTimeIsNullAndReturnTimeIsNullOrderByIdAsc(terminal.getId());

        if (pendingOpt.isEmpty()) {
            return "OK";
        }

        TerminalCommand pending = pendingOpt.get();
        pending.setTransferTime(OffsetDateTime.now());
        terminalCommandRepository.save(pending);

        return "C:" + pending.getId() + ":" + pending.getContent();
    }

    public String handleDeviceCmd(Map<String, String> params, String body, String remoteIp, boolean pullCommandIfNoResult) {
        String sn = required(params, "SN");
        Terminal terminal = touchTerminal(sn, params, remoteIp);

        boolean hasResult = (body != null && !body.isBlank())
                || params.containsKey("ID")
                || params.containsKey("CmdId")
                || params.containsKey("Return");

        if (!hasResult && pullCommandIfNoResult) {
            return handleGetRequest(params, remoteIp);
        }

        int updated = saveCommandResult(terminal, body, params);
        return "OK:" + updated;
    }

    public TerminalCommandResponse queueCommand(String sn, String content) {
        if (content == null || content.isBlank()) {
            throw new BadRequestException("Command content is required");
        }

        Terminal terminal = terminalRepository.findBySn(sn)
                .orElseThrow(() -> new NotFoundException("Terminal not found: " + sn));

        TerminalCommand command = new TerminalCommand();
        command.setTerminal(terminal);
        command.setContent(content.trim());
        command.setCommitTime(OffsetDateTime.now());

        TerminalCommand saved = terminalCommandRepository.save(command);
        return mapCommand(saved);
    }

    public TerminalCommandResponse queueTerminalCommand(String sn, String content) {
        return queueCommand(sn, content);
    }

    public TerminalCommandResponse queueReadInfo(String sn) {
        return queueCommand(sn, "INFO");
    }

    public TerminalCommandResponse queueCheckAll(String sn) {
        return queueCommand(sn, "CHECK ALL");
    }

    public TerminalCommandResponse queueReboot(String sn) {
        Terminal terminal = terminalRepository.findBySn(sn)
                .orElseThrow(() -> new NotFoundException("Terminal not found: " + sn));
        String cmd = terminal.getProductType() != null && (terminal.getProductType() == 5
                || terminal.getProductType() == 15 || terminal.getProductType() == 25)
                ? "CONTROL DEVICE 0300"
                : "REBOOT";
        return queueCommand(sn, cmd);
    }

    public TerminalCommandResponse queueClearData(String sn) {
        return queueCommand(sn, "CLEAR DATA");
    }

    public TerminalCommandResponse queueClearLog(String sn) {
        return queueCommand(sn, "CLEAR LOG");
    }

    public TerminalCommandResponse queueSetOption(String sn, String option, String value) {
        if (option == null || option.isBlank()) {
            throw new BadRequestException("option is required");
        }
        String safeOption = option.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim();
        String safeValue = value == null ? "" : value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim();
        return queueCommand(sn, "SET OPTION " + safeOption + "=" + safeValue);
    }

    public TerminalResponse setTerminalEnabled(String sn, boolean enabled) {
        Terminal terminal = terminalRepository.findBySn(sn)
                .orElseThrow(() -> new NotFoundException("Terminal not found: " + sn));
        terminal.setState(enabled ? 1 : 0);
        return mapTerminal(terminalRepository.save(terminal));
    }

    @Transactional(readOnly = true)
    public List<TerminalResponse> listTerminals() {
        return terminalRepository.findAll().stream()
                .sorted(Comparator.comparing(Terminal::getId))
                .map(this::mapTerminal)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TerminalCommandResponse> listCommands(String sn) {
        Terminal terminal = terminalRepository.findBySn(sn)
                .orElseThrow(() -> new NotFoundException("Terminal not found: " + sn));

        return terminalCommandRepository.findByTerminalIdOrderByIdAsc(terminal.getId()).stream()
                .map(this::mapCommand)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceTransactionResponse> listRecentAttendance(String terminalSn, String empCode, Integer limit) {
        int max = limit == null ? 50 : Math.max(1, Math.min(limit, 200));
        String terminalFilter = terminalSn == null || terminalSn.isBlank() ? null : terminalSn.trim();
        String empFilter = empCode == null || empCode.isBlank() ? null : empCode.trim();

        List<AttendanceTransaction> rows;
        if (terminalFilter != null && empFilter != null) {
            rows = attendanceTransactionRepository.findTop200ByTerminalSnAndEmpCodeOrderByPunchTimeDescIdDesc(terminalFilter, empFilter);
        } else if (terminalFilter != null) {
            rows = attendanceTransactionRepository.findTop200ByTerminalSnOrderByPunchTimeDescIdDesc(terminalFilter);
        } else if (empFilter != null) {
            rows = attendanceTransactionRepository.findTop200ByEmpCodeOrderByPunchTimeDescIdDesc(empFilter);
        } else {
            rows = attendanceTransactionRepository.findTop200ByOrderByPunchTimeDescIdDesc();
        }

        return rows.stream()
                .limit(max)
                .map(this::mapAttendance)
                .toList();
    }

    private Terminal touchTerminal(String sn, Map<String, String> params, String remoteIp) {
        Terminal terminal = terminalRepository.findBySn(sn).orElseGet(() -> createTerminal(sn));

        if (params.containsKey("alias")) {
            String alias = params.get("alias");
            if (alias != null && !alias.isBlank()) {
                terminal.setAlias(alias.trim());
            }
        }

        if (params.containsKey("DeviceType")) {
            terminal.setProductType(mapProductType(params.get("DeviceType")));
        }

        if (params.containsKey("pushver")) {
            String pushVer = params.get("pushver");
            if (pushVer != null && !pushVer.isBlank()) {
                terminal.setPushVer(pushVer.trim());
            }
        }

        if (params.containsKey("PIN") && terminal.getIsRegistration() == null) {
            terminal.setIsRegistration((short) 1);
        }

        if (remoteIp != null && !remoteIp.isBlank()) {
            terminal.setRealIp(remoteIp);
            if (terminal.getIpAddress() == null || terminal.getIpAddress().isBlank()) {
                terminal.setIpAddress(remoteIp);
            }
        }

        terminal.setLastActivity(OffsetDateTime.now());
        if (terminal.getState() == null) {
            terminal.setState(1);
        }

        return terminalRepository.save(terminal);
    }

    private Terminal createTerminal(String sn) {
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

    private void applyDeviceInfo(Terminal terminal, Map<String, String> fields) {
        putIfPresent(fields, "FWVersion", terminal::setFwVer);
        putIfPresent(fields, "PushVersion", terminal::setPushVer);
        putIfPresent(fields, "~Platform", terminal::setPlatform);
        putIfPresent(fields, "~DeviceName", terminal::setTerminalName);
        putIfPresent(fields, "~OEMVendor", terminal::setOemVendor);
        putIfPresentInt(fields, "UserCount", terminal::setUserCount);
        putIfPresentInt(fields, "TransactionCount", terminal::setTransactionCount);
        putIfPresentInt(fields, "FPCount", terminal::setFpCount);
        putIfPresentInt(fields, "FaceCount", terminal::setFaceCount);
        putIfPresentInt(fields, "FvCount", terminal::setFvCount);
        putIfPresentInt(fields, "PvCount", terminal::setPalmCount);
        putIfPresent(fields, "~ZKFPVersion", terminal::setFpAlgVer);
        putIfPresent(fields, "ZKFaceVersion", terminal::setFaceAlgVer);
        putIfPresent(fields, "FvVersion", terminal::setFvAlgVer);
        putIfPresent(fields, "PvVersion", terminal::setPalmAlgVer);

        String ipAddress = fields.get("IPAddress");
        if (ipAddress != null && !ipAddress.isBlank()) {
            terminal.setIpAddress(ipAddress.trim());
        }
    }

    private int ingestAttendanceLog(Terminal terminal, String body) {
        if (body == null || body.isBlank()) {
            return 0;
        }

        int count = 0;
        for (String rawLine : splitLines(body)) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("ID=") || line.startsWith("OPLOG")) {
                continue;
            }
            Optional<AttendanceTransaction> txOpt = parseAttendance(terminal, line);
            if (txOpt.isEmpty()) {
                continue;
            }
            AttendanceTransaction tx = txOpt.get();
            try {
                if (!attendanceTransactionRepository.existsByCompanyCodeAndEmpCodeAndPunchTime(
                        tx.getCompanyCode(), tx.getEmpCode(), tx.getPunchTime())) {
                    attendanceTransactionRepository.save(tx);
                    count++;
                }
            } catch (DataIntegrityViolationException ignored) {
                // Duplicate rows from retry uploads should not break device communication.
            }
        }

        terminal.setUploadTime(OffsetDateTime.now());
        terminalRepository.save(terminal);

        return count;
    }

    private Optional<AttendanceTransaction> parseAttendance(Terminal terminal, String line) {
        String[] cols = line.split("\\t");
        if (cols.length < 2) {
            return Optional.empty();
        }

        String pin = cols[0].trim();
        if (pin.isEmpty()) {
            return Optional.empty();
        }

        ParsedPunchTime parsedTime = parsePunchTime(cols[1].trim());
        if (parsedTime == null) {
            return Optional.empty();
        }

        Employee employee = resolveEmployee(terminal, pin);
        String companyCode = null;
        if (employee != null && employee.getCompany() != null) {
            companyCode = employee.getCompany().getCompanyCode();
        } else if (terminal.getArea() != null && terminal.getArea().getCompany() != null) {
            companyCode = terminal.getArea().getCompany().getCompanyCode();
        }

        AttendanceTransaction tx = new AttendanceTransaction();
        tx.setCompanyCode(companyCode);
        tx.setEmpCode(pin);
        tx.setEmployee(employee);
        tx.setPunchTime(parsedTime.punchTime());
        tx.setPunchState(valueOrDefault(cols, 2, "255"));
        tx.setVerifyType(parseInt(valueOrDefault(cols, 3, "0"), 0));
        tx.setWorkCode(blankToNull(valueOrDefault(cols, 4, null)));
        tx.setReserved(blankToNull(valueOrDefault(cols, 5, null)));
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
        tx.setUploadTime(OffsetDateTime.now());
        tx.setSyncStatus((short) 0);

        String maskFlag = valueOrDefault(cols, 7, null);
        tx.setIsMask((short) parseInt(maskFlag, 255).intValue());

        String tempVal = valueOrDefault(cols, 8, null);
        tx.setTemperature(parseDecimal(tempVal, new BigDecimal("255.0")));

        return Optional.of(tx);
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

    private int saveCommandResult(Terminal terminal, String body, Map<String, String> params) {
        List<Map<String, String>> payloads = new ArrayList<>();

        if (body != null && !body.isBlank()) {
            for (String line : splitLines(body)) {
                Map<String, String> fields = parseKvPayload(line);
                if (!fields.isEmpty()) {
                    payloads.add(fields);
                }
            }
        }

        if (payloads.isEmpty() && !params.isEmpty()) {
            payloads.add(new LinkedHashMap<>(params));
        }

        int updated = 0;
        for (Map<String, String> fields : payloads) {
            String idText = firstNonBlank(fields.get("ID"), fields.get("CmdId"), fields.get("CMDID"));
            if (idText == null) {
                continue;
            }
            Long id = parseLong(idText, null);
            if (id == null) {
                continue;
            }
            Integer returnCode = parseInt(firstNonBlank(
                    fields.get("Return"),
                    fields.get("ReturnValue"),
                    fields.get("return_value"),
                    fields.get("RET")
            ), 0);

            Optional<TerminalCommand> cmdOpt = terminalCommandRepository.findById(id);
            if (cmdOpt.isEmpty()) {
                continue;
            }
            TerminalCommand cmd = cmdOpt.get();
            if (!cmd.getTerminal().getId().equals(terminal.getId())) {
                continue;
            }
            boolean firstResult = cmd.getReturnTime() == null;
            cmd.setReturnValue(returnCode);
            cmd.setReturnTime(OffsetDateTime.now());
            terminalCommandRepository.save(cmd);
            if (firstResult) {
                handleCommandResultSideEffects(terminal, cmd, fields, returnCode);
            }
            updated++;
        }

        return updated;
    }

    private void handleCommandResultSideEffects(
            Terminal terminal,
            TerminalCommand command,
            Map<String, String> fields,
            Integer returnCode
    ) {
        if (returnCode != null && returnCode != 0) {
            return;
        }
        String content = command.getContent();
        if (content == null || content.isBlank()) {
            return;
        }
        if (content.startsWith("VERIFY SUM ATTLOG ")) {
            handleAttendanceProofResult(terminal, fields, content);
        }
    }

    private void handleAttendanceProofResult(Terminal terminal, Map<String, String> fields, String commandContent) {
        OffsetDateTime startTime = parseOffsetDateTime(firstNonBlank(
                fields.get("StartTime"),
                fields.get("start_time"),
                extractCommandRangeValue(commandContent, "StartTime=")
        ));
        OffsetDateTime endTime = parseOffsetDateTime(firstNonBlank(
                fields.get("EndTime"),
                fields.get("end_time"),
                extractCommandRangeValue(commandContent, "EndTime=")
        ));
        Integer terminalCount = parseInt(firstNonBlank(
                fields.get("AttlogSum"),
                fields.get("AttlogCount"),
                fields.get("attlog_sum"),
                fields.get("attlog_count")
        ), null);

        if (startTime == null || endTime == null || terminalCount == null) {
            return;
        }

        int serverCount = (int) attendanceTransactionRepository.countByTerminalSnAndPunchTimeBetween(
                terminal.getSn(),
                startTime,
                endTime
        );
        short flag = 0;
        if (serverCount < terminalCount) {
            queueFollowUpAttendanceQuery(terminal, startTime, endTime);
            flag = -1;
        }

        TransactionProofCommand proof = new TransactionProofCommand();
        proof.setTerminal(terminal);
        proof.setActionTime(OffsetDateTime.now());
        proof.setStartTime(startTime);
        proof.setEndTime(endTime);
        proof.setTerminalCount(terminalCount);
        proof.setServerCount(serverCount);
        proof.setFlag(flag);
        proof.setReservedInit(serverCount - terminalCount);
        proof.setReservedChar(truncate(commandContent, 30));
        transactionProofCommandRepository.save(proof);
    }

    private void queueFollowUpAttendanceQuery(Terminal terminal, OffsetDateTime startTime, OffsetDateTime endTime) {
        TerminalCommand command = new TerminalCommand();
        command.setTerminal(terminal);
        command.setContent("DATA QUERY ATTLOG StartTime=" + formatCommandTime(startTime)
                + "\tEndTime=" + formatCommandTime(endTime));
        command.setCommitTime(OffsetDateTime.now());
        terminalCommandRepository.save(command);
    }

    private String extractCommandRangeValue(String command, String prefix) {
        if (command == null || prefix == null) {
            return null;
        }
        int start = command.indexOf(prefix);
        if (start < 0) {
            return null;
        }
        start += prefix.length();
        int end = command.indexOf('\t', start);
        if (end < 0) {
            end = command.length();
        }
        return blankToNull(command.substring(start, end));
    }

    private boolean isRegistrationTable(String table) {
        if (table == null) {
            return false;
        }
        return switch (table) {
            case "USER", "USERINFO", "FP", "FINGERTMP", "BIODATA" -> true;
            default -> false;
        };
    }

    private int ingestRegistrationTable(Terminal terminal, String table, String body) {
        if (body == null || body.isBlank()) {
            return 0;
        }
        return switch (table) {
            case "USER", "USERINFO" -> ingestUserInfoRows(terminal, body, table);
            case "FP", "FINGERTMP" -> ingestFingerRows(terminal, body, table);
            case "BIODATA" -> ingestBioDataRows(terminal, body);
            default -> 0;
        };
    }

    private int ingestPrefixedRegistrationLines(Terminal terminal, String body) {
        if (body == null || body.isBlank()) {
            return 0;
        }
        int count = 0;
        for (String line : splitLines(body)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("USER ")) {
                count += upsertUserFromFields(terminal, parseKvPayload(trimmed.substring(5)));
            } else if (trimmed.startsWith("FP ")) {
                count += upsertFingerprintFromFields(terminal, parseKvPayload(trimmed.substring(3)));
            } else if (trimmed.startsWith("BIODATA ")) {
                count += upsertBioDataFromFields(terminal, parseKvPayload(trimmed.substring(8)));
            }
        }
        return count;
    }

    private int ingestUserInfoRows(Terminal terminal, String body, String table) {
        int count = 0;
        for (String line : splitLines(body)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String payload = trimmed;
            if (trimmed.startsWith("USER ")) {
                payload = trimmed.substring(5);
            } else if (trimmed.startsWith("USERINFO ")) {
                payload = trimmed.substring(9);
            }
            count += upsertUserFromFields(terminal, parseKvPayload(payload));
        }
        return count;
    }

    private int ingestFingerRows(Terminal terminal, String body, String table) {
        int count = 0;
        for (String line : splitLines(body)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String payload = trimmed.startsWith("FP ") ? trimmed.substring(3) : trimmed;
            count += upsertFingerprintFromFields(terminal, parseKvPayload(payload));
        }
        return count;
    }

    private int ingestBioDataRows(Terminal terminal, String body) {
        int count = 0;
        for (String line : splitLines(body)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String payload = trimmed.startsWith("BIODATA ") ? trimmed.substring(8) : trimmed;
            count += upsertBioDataFromFields(terminal, parseKvPayload(payload));
        }
        return count;
    }

    private int upsertUserFromFields(Terminal terminal, Map<String, String> fields) {
        if (fields == null || fields.isEmpty()) {
            return 0;
        }
        String pin = firstNonBlank(fields.get("PIN"), fields.get("Pin"));
        if (pin == null) {
            return 0;
        }
        Employee employee = resolveEmployee(terminal, pin);
        if (employee == null) {
            employee = createEmployeeForDeviceUpload(terminal, pin, fields);
            if (employee == null) {
                return 0;
            }
        }

        String name = blankToNull(firstNonBlank(fields.get("Name"), fields.get("name")));
        if (name != null) {
            employee.setFirstName(name);
        }
        String passwd = firstNonBlank(fields.get("Passwd"), fields.get("Password"));
        if (passwd != null) {
            employee.setDevicePassword("0".equals(passwd) ? null : passwd);
        }
        String card = normalizeDeviceCard(firstNonBlank(fields.get("Card"), fields.get("CARD")));
        if (card != null) {
            employee.setCardNo(card);
        }
        employee.setDevPrivilege(parseInt(firstNonBlank(fields.get("Pri"), fields.get("Privilege")), employee.getDevPrivilege() != null ? employee.getDevPrivilege() : 0));
        Integer verify = parseInt(firstNonBlank(fields.get("Verify"), fields.get("VERIFY")), employee.getVerifyMode() != null ? employee.getVerifyMode() : 0);
        if (verify != null && verify == -1) {
            verify = 0;
        }
        employee.setVerifyMode(verify);
        employee.setEnrollSn(terminal.getSn());
        employee.setUpdateTime(OffsetDateTime.now());

        if (terminal.getArea() != null) {
            employee.getAreas().add(terminal.getArea());
        }
        employeeRepository.save(employee);
        return 1;
    }

    private int upsertFingerprintFromFields(Terminal terminal, Map<String, String> fields) {
        if (fields == null || fields.isEmpty()) {
            return 0;
        }
        String pin = firstNonBlank(fields.get("PIN"), fields.get("Pin"));
        String template = blankToNull(firstNonBlank(fields.get("TMP"), fields.get("Tmp")));
        Integer fid = parseInt(firstNonBlank(fields.get("FID"), fields.get("No")), null);
        if (pin == null || template == null || fid == null) {
            return 0;
        }
        Employee employee = resolveEmployee(terminal, pin);
        if (employee == null) {
            employee = createEmployeeForDeviceUpload(terminal, pin, fields);
            if (employee == null) {
                return 0;
            }
        }
        String majorVer = blankToNull(terminal.getFpAlgVer());
        if (majorVer == null) {
            majorVer = "10";
        }

        upsertBioTemplate(
                employee.getId(),
                terminal.getSn(),
                1,
                fid,
                0,
                0,
                majorVer,
                "0",
                template,
                parseInt(fields.get("Valid"), 1),
                parseInt(fields.get("Duress"), 0)
        );
        employee.setEnrollSn(terminal.getSn());
        employee.setUpdateTime(OffsetDateTime.now());
        employeeRepository.save(employee);
        return 1;
    }

    private int upsertBioDataFromFields(Terminal terminal, Map<String, String> fields) {
        if (fields == null || fields.isEmpty()) {
            return 0;
        }
        String pin = firstNonBlank(fields.get("PIN"), fields.get("Pin"));
        String template = blankToNull(firstNonBlank(fields.get("Tmp"), fields.get("TMP")));
        Integer type = parseInt(firstNonBlank(fields.get("Type"), fields.get("TYPE")), null);
        Integer no = parseInt(firstNonBlank(fields.get("No"), fields.get("FID")), null);
        Integer index = parseInt(firstNonBlank(fields.get("Index"), fields.get("IDX")), 0);
        Integer format = parseInt(firstNonBlank(fields.get("Format"), fields.get("format")), 0);
        String majorVer = blankToNull(firstNonBlank(fields.get("MajorVer"), fields.get("major_ver")));
        String minorVer = blankToNull(firstNonBlank(fields.get("MinorVer"), fields.get("minor_ver")));
        if (pin == null || template == null || type == null || no == null || majorVer == null) {
            return 0;
        }
        Employee employee = resolveEmployee(terminal, pin);
        if (employee == null) {
            employee = createEmployeeForDeviceUpload(terminal, pin, fields);
            if (employee == null) {
                return 0;
            }
        }

        upsertBioTemplate(
                employee.getId(),
                terminal.getSn(),
                type,
                no,
                index != null ? index : 0,
                format != null ? format : 0,
                majorVer,
                minorVer != null ? minorVer : "0",
                template,
                parseInt(firstNonBlank(fields.get("Valid"), fields.get("valid")), 1),
                parseInt(firstNonBlank(fields.get("Duress"), fields.get("duress")), 0)
        );
        employee.setEnrollSn(terminal.getSn());
        employee.setUpdateTime(OffsetDateTime.now());
        employeeRepository.save(employee);
        return 1;
    }

    private void upsertBioTemplate(
            Long employeeId,
            String sn,
            Integer bioType,
            Integer bioNo,
            Integer bioIndex,
            Integer bioFormat,
            String majorVer,
            String minorVer,
            String bioTmp,
            Integer valid,
            Integer duress
    ) {
        BioDataTemplate tpl = bioDataTemplateRepository
                .findByEmployeeIdAndBioNoAndBioIndexAndBioTypeAndBioFormatAndMajorVer(
                        employeeId, bioNo, bioIndex, bioType, bioFormat, majorVer
                )
                .orElseGet(BioDataTemplate::new);

        tpl.setEmployeeId(employeeId);
        tpl.setBioNo(bioNo);
        tpl.setBioIndex(bioIndex);
        tpl.setBioType(bioType);
        tpl.setBioFormat(bioFormat);
        tpl.setMajorVer(majorVer);
        tpl.setMinorVer(minorVer);
        tpl.setBioTmp(bioTmp);
        tpl.setValid(valid != null ? valid : 1);
        tpl.setDuress(duress != null ? duress : 0);
        tpl.setSn(sn);
        tpl.setUpdateTime(OffsetDateTime.now());
        bioDataTemplateRepository.save(tpl);
    }

    private Employee createEmployeeForDeviceUpload(Terminal terminal, String pin, Map<String, String> fields) {
        Company company = null;
        Area area = terminal.getArea();
        if (area != null) {
            company = area.getCompany();
        }
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
        if (employee.getEmpCode().chars().allMatch(Character::isDigit) && employee.getEmpCode().length() <= 18) {
            try {
                employee.setEmpCodeDigit(Long.parseLong(employee.getEmpCode()));
            } catch (NumberFormatException ignored) {
                employee.setEmpCodeDigit(null);
            }
        }
        employee.setFirstName(blankToNull(firstNonBlank(fields.get("Name"), fields.get("name"))));
        employee.setDevPrivilege(0);
        employee.setVerifyMode(0);
        employee.setHireDate(LocalDate.now());
        employee.setEnablePayroll(true);
        employee.setAppStatus((short) 0);
        employee.setAppRole((short) 1);
        employee.setUpdateTime(OffsetDateTime.now());
        employee.setEnrollSn(terminal.getSn());
        if (area != null) {
            employee.getAreas().add(area);
        }
        return employeeRepository.save(employee);
    }

    private String normalizeDeviceCard(String card) {
        if (card == null) {
            return null;
        }
        String trimmed = card.trim();
        if (trimmed.isEmpty() || "0".equals(trimmed) || "[]".equals(trimmed) || "[0000000000]".equals(trimmed)) {
            return null;
        }
        return trimmed.replaceFirst("^0+(?!$)", "");
    }

    private boolean isCommandResultPayload(String body) {
        if (body == null || body.isBlank()) {
            return false;
        }
        String upper = body.toUpperCase(Locale.ROOT);
        return upper.contains("ID=") || upper.contains("CMD=") || upper.contains("RETURN=");
    }

    private ParsedPunchTime parsePunchTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String rawTime = value;
        Double lon = null;
        Double lat = null;

        if (value.contains(",")) {
            String[] parts = value.split(",");
            rawTime = parts[0].trim();
            if (parts.length > 2) {
                lon = parseDouble(parts[1], null);
                lat = parseDouble(parts[2], null);
            }
        }

        try {
            LocalDateTime parsed = LocalDateTime.parse(rawTime, PUNCH_TIME_FORMAT);
            return new ParsedPunchTime(parsed.atOffset(ZoneOffset.UTC), lon, lat);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private void persistTerminalParameters(Terminal terminal, Map<String, String> fields) {
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String key = blankToNull(entry.getKey());
            String value = blankToNull(entry.getValue());
            if (key == null || value == null) {
                continue;
            }
            TerminalParameter parameter = terminalParameterRepository
                    .findByTerminalIdAndParamName(terminal.getId(), key)
                    .orElseGet(TerminalParameter::new);
            parameter.setTerminal(terminal);
            parameter.setParamName(key);
            parameter.setParamValue(truncate(value, 100));
            terminalParameterRepository.save(parameter);
        }
    }

    private void updateLogStamp(Terminal terminal, String stamp) {
        if (stamp == null || stamp.isBlank()) {
            return;
        }
        terminal.setLogStamp(stamp.trim());
        terminalRepository.save(terminal);
    }

    private void updateOpLogStamp(Terminal terminal, String stamp) {
        if (stamp == null || stamp.isBlank()) {
            return;
        }
        terminal.setOpLogStamp(stamp.trim());
        terminalRepository.save(terminal);
    }

    private void updateCaptureStamp(Terminal terminal, String stamp) {
        if (stamp == null || stamp.isBlank()) {
            return;
        }
        terminal.setCaptureStamp(stamp.trim());
        terminalRepository.save(terminal);
    }

    private int ingestOperationLogs(Terminal terminal, String body, Map<String, String> params) {
        int saved = 0;
        List<String> candidates = body != null && !body.isBlank() ? splitLines(body) : List.of();
        if (candidates.isEmpty() && !params.isEmpty()) {
            candidates = List.of(String.join("\t", params.values()));
        }

        for (String rawLine : candidates) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.contains("=")) {
                continue;
            }
            String[] cols = line.split("\\t");
            if (cols.length < 7) {
                continue;
            }
            OffsetDateTime actionTime = parseOffsetDateTime(cols[2]);
            if (actionTime == null) {
                continue;
            }
            if (terminalLogRepository.findFirstByTerminalIdAndActionTime(terminal.getId(), actionTime).isPresent()) {
                continue;
            }

            TerminalLog log = new TerminalLog();
            log.setTerminal(terminal);
            log.setTerminalTz(terminal.getTerminalTz());
            log.setAdmin(blankToNull(cols[1]));
            log.setActionName(parseShort(cols[0], null));
            log.setActionTime(actionTime);
            log.setObject(blankToNull(cols[3]));
            log.setParam1(parseInt(cols[4], null));
            log.setParam2(parseInt(cols[5], null));
            log.setParam3(parseInt(cols[6], null));
            log.setUploadTime(OffsetDateTime.now());
            terminalLogRepository.save(log);
            saved++;
        }

        return saved;
    }

    private int ingestErrorLogs(Terminal terminal, String body, Map<String, String> params) {
        int saved = 0;
        List<Map<String, String>> payloads = new ArrayList<>();
        if (body != null && !body.isBlank()) {
            for (String line : splitLines(body)) {
                String normalized = line.trim();
                if (normalized.regionMatches(true, 0, "ERRORLOG ", 0, 9)) {
                    normalized = normalized.substring(9);
                }
                Map<String, String> fields = parseKvPayload(normalized);
                if (!fields.isEmpty()) {
                    payloads.add(fields);
                }
            }
        }
        if (payloads.isEmpty() && !params.isEmpty()) {
            payloads.add(new LinkedHashMap<>(params));
        }

        for (Map<String, String> fields : payloads) {
            ErrorCommandLog log = new ErrorCommandLog();
            log.setTerminal(terminal);
            log.setErrorCode(truncate(blankToNull(firstNonBlank(fields.get("ErrCode"), fields.get("error_code"))), 16));
            log.setErrorMsg(truncate(blankToNull(firstNonBlank(fields.get("ErrMsg"), fields.get("error_msg"))), 50));
            log.setDataOrigin(blankToNull(firstNonBlank(fields.get("DataOrigin"), fields.get("data_origin"))));
            log.setCmd(truncate(blankToNull(firstNonBlank(fields.get("CmdId"), fields.get("cmd"))), 50));
            log.setAdditional(decodeBase64(firstNonBlank(fields.get("Additional"), fields.get("additional"))));
            log.setUploadTime(OffsetDateTime.now());
            errorCommandLogRepository.save(log);
            saved++;
        }

        return saved;
    }

    private MixedUploadResult ingestMixedBody(Terminal terminal, String body, Map<String, String> params) {
        if (body == null || body.isBlank()) {
            return new MixedUploadResult(0, 0, 0, 0);
        }

        int attendance = 0;
        int registration = 0;
        int operation = 0;
        int error = 0;
        List<String> registrationLines = new ArrayList<>();
        List<String> attendanceLines = new ArrayList<>();
        List<String> operationLines = new ArrayList<>();
        List<String> errorLines = new ArrayList<>();

        for (String rawLine : splitLines(body)) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.regionMatches(true, 0, "ATTLOG ", 0, 7)) {
                attendanceLines.add(line);
            } else if (line.regionMatches(true, 0, "ERRORLOG ", 0, 9)) {
                errorLines.add(line);
            } else if (line.regionMatches(true, 0, "USER ", 0, 5)
                    || line.regionMatches(true, 0, "USERINFO ", 0, 9)
                    || line.regionMatches(true, 0, "FP ", 0, 3)
                    || line.regionMatches(true, 0, "BIODATA ", 0, 8)) {
                registrationLines.add(line);
            } else if (!line.contains("=") && line.split("\\t").length >= 7) {
                operationLines.add(line);
            }
        }

        if (!attendanceLines.isEmpty()) {
            AttendanceSyncResponse sync = attendanceSyncService.syncFromDevice(terminal, String.join("\n", attendanceLines));
            attendance = sync.savedCount();
        }
        if (!registrationLines.isEmpty()) {
            registration = ingestPrefixedRegistrationLines(terminal, String.join("\n", registrationLines));
        }
        if (!operationLines.isEmpty()) {
            operation = ingestOperationLogs(terminal, String.join("\n", operationLines), params);
            if (operation > 0) {
                saveProtocolUploadLog(terminal, "OPLOG", "mixed-operation-log", operation, 0);
            }
        }
        if (!errorLines.isEmpty()) {
            error = ingestErrorLogs(terminal, String.join("\n", errorLines), params);
            if (error > 0) {
                saveProtocolUploadLog(terminal, "ERRORLOG", "mixed-error-log", error, 0);
            }
        }

        return new MixedUploadResult(attendance, registration, operation, error);
    }

    private void saveProtocolUploadLog(Terminal terminal, String event, String content, int uploadCount, int errorCount) {
        TerminalUploadLog uploadLog = new TerminalUploadLog();
        uploadLog.setTerminal(terminal);
        uploadLog.setEvent(truncate(event, 80));
        uploadLog.setContent(truncate(content, 80));
        uploadLog.setUploadCount(uploadCount);
        uploadLog.setErrorCount(errorCount);
        uploadLog.setUploadTime(OffsetDateTime.now());
        terminalUploadLogRepository.save(uploadLog);
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

    private String required(Map<String, String> params, String key) {
        String value = params.get(key);
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Missing required parameter: " + key);
        }
        return value.trim();
    }

    private Short mapProductType(String deviceType) {
        if (deviceType == null) {
            return 9;
        }
        return switch (deviceType.trim().toLowerCase(Locale.ROOT)) {
            case "ins" -> (short) 8;
            case "acc" -> (short) 5;
            case "att" -> (short) 9;
            case "ai" -> (short) 31;
            default -> (short) 9;
        };
    }

    private String normalizeUpper(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
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

    private void putIfPresent(Map<String, String> map, String key, java.util.function.Consumer<String> consumer) {
        String val = map.get(key);
        if (val != null && !val.isBlank()) {
            consumer.accept(val.trim());
        }
    }

    private void putIfPresentInt(Map<String, String> map, String key, java.util.function.Consumer<Integer> consumer) {
        String val = map.get(key);
        if (val != null && !val.isBlank()) {
            consumer.accept(parseInt(val, 0));
        }
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

    private Short parseShort(String value, Short defaultValue) {
        Integer parsed = parseInt(value, null);
        return parsed == null ? defaultValue : parsed.shortValue();
    }

    private Long parseLong(String value, Long defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private OffsetDateTime parseOffsetDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            String normalized = value.trim();
            if (normalized.length() < 19) {
                return null;
            }
            LocalDateTime parsed = LocalDateTime.parse(normalized.substring(0, 19), PUNCH_TIME_FORMAT);
            return parsed.atOffset(ZoneOffset.UTC);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String formatCommandTime(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().format(PUNCH_TIME_FORMAT);
    }

    private String decodeBase64(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new String(Base64.getDecoder().decode(value.trim()), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return value;
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
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

    private TerminalResponse mapTerminal(Terminal terminal) {
        return new TerminalResponse(
                terminal.getId(),
                terminal.getSn(),
                terminal.getAlias(),
                terminal.getIpAddress(),
                terminal.getRealIp(),
                terminal.getProductType(),
                terminal.getIsAttendance(),
                terminal.getIsRegistration(),
                terminal.getLastActivity(),
                terminal.getArea() != null ? terminal.getArea().getId() : null,
                terminal.getArea() != null ? terminal.getArea().getAreaName() : null
        );
    }

    private TerminalCommandResponse mapCommand(TerminalCommand command) {
        return new TerminalCommandResponse(
                command.getId(),
                command.getTerminal().getSn(),
                command.getContent(),
                command.getCommitTime(),
                command.getTransferTime(),
                command.getReturnTime(),
                command.getReturnValue()
        );
    }

    private AttendanceTransactionResponse mapAttendance(AttendanceTransaction tx) {
        return new AttendanceTransactionResponse(
                tx.getId(),
                tx.getTerminalSn(),
                tx.getTerminalAlias(),
                tx.getAreaAlias(),
                tx.getEmpCode(),
                tx.getEmployee() != null ? tx.getEmployee().getId() : null,
                tx.getEmployee() != null ? tx.getEmployee().getFirstName() : null,
                tx.getEmployee() != null ? tx.getEmployee().getLastName() : null,
                tx.getPunchTime(),
                tx.getPunchState(),
                tx.getVerifyType(),
                tx.getWorkCode(),
                tx.getLongitude(),
                tx.getLatitude(),
                tx.getIsMask(),
                tx.getTemperature(),
                tx.getUploadTime()
        );
    }

    private record MixedUploadResult(int attendance, int registration, int operation, int error) {
        private int totalProcessed() {
            return attendance + registration + operation + error;
        }
    }

    private record ParsedPunchTime(OffsetDateTime punchTime, Double longitude, Double latitude) {
    }
}
