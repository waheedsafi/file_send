package Zkteco.zkteco.modules.employee.service;

import Zkteco.zkteco.domain.iclock.BioDataTemplate;
import Zkteco.zkteco.domain.iclock.Terminal;
import Zkteco.zkteco.domain.iclock.TerminalCommand;
import Zkteco.zkteco.domain.personnel.Area;
import Zkteco.zkteco.domain.personnel.Employee;
import Zkteco.zkteco.modules.job.service.AsyncJobService;
import Zkteco.zkteco.modules.employee.dto.EmployeeRemoteEnrollRequest;
import Zkteco.zkteco.repository.iclock.BioDataTemplateRepository;
import Zkteco.zkteco.repository.iclock.TerminalCommandRepository;
import Zkteco.zkteco.repository.iclock.TerminalRepository;
import Zkteco.zkteco.repository.personnel.EmployeeRepository;
import Zkteco.zkteco.web.error.NotFoundException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeDeviceActionWorker {

    private final AsyncJobService asyncJobService;
    private final EmployeeRepository employeeRepository;
    private final TerminalRepository terminalRepository;
    private final TerminalCommandRepository terminalCommandRepository;
    private final BioDataTemplateRepository bioDataTemplateRepository;

    public EmployeeDeviceActionWorker(
            AsyncJobService asyncJobService,
            EmployeeRepository employeeRepository,
            TerminalRepository terminalRepository,
            TerminalCommandRepository terminalCommandRepository,
            BioDataTemplateRepository bioDataTemplateRepository
    ) {
        this.asyncJobService = asyncJobService;
        this.employeeRepository = employeeRepository;
        this.terminalRepository = terminalRepository;
        this.terminalCommandRepository = terminalCommandRepository;
        this.bioDataTemplateRepository = bioDataTemplateRepository;
    }

    @Async("deviceActionExecutor")
    @Transactional
    public void syncSingleEmployee(Long jobId, Long employeeId, List<String> terminalSns) {
        processJob(jobId, () -> {
            Employee employee = findEmployee(employeeId);
            List<Terminal> terminals = resolveTerminalsForEmployee(employee, terminalSns);
            int total = queueSyncCommands(List.of(employee), terminals);
            return "queuedCommands=" + total;
        });
    }

    @Async("deviceActionExecutor")
    @Transactional
    public void pullSingleEmployee(Long jobId, Long employeeId, List<String> terminalSns) {
        processJob(jobId, () -> {
            Employee employee = findEmployee(employeeId);
            List<Terminal> terminals = resolveTerminalsForEmployee(employee, terminalSns);
            int total = 0;
            for (Terminal terminal : terminals) {
                total += queueCommand(terminal, "DATA QUERY USERINFO PIN=" + sanitize(employee.getEmpCode()));
            }
            return "queuedCommands=" + total;
        });
    }

    @Async("deviceActionExecutor")
    @Transactional
    public void syncEmployeeBatch(Long jobId, List<Long> employeeIds, List<String> terminalSns) {
        processJob(jobId, () -> {
            List<Employee> employees;
            if (employeeIds == null || employeeIds.isEmpty()) {
                employees = employeeRepository.findAll();
            } else {
                employees = employeeRepository.findAllById(employeeIds);
            }
            if (employees.isEmpty()) {
                return "queuedCommands=0";
            }

            List<Terminal> terminals = resolveTerminalsForEmployees(employees, terminalSns);
            int total = queueSyncCommands(employees, terminals);
            return "queuedCommands=" + total;
        });
    }

    @Async("deviceActionExecutor")
    @Transactional
    public void pullEmployeeBatch(Long jobId, List<Long> employeeIds, List<String> terminalSns) {
        processJob(jobId, () -> {
            List<Employee> employees = employeeIds == null || employeeIds.isEmpty()
                    ? employeeRepository.findAll()
                    : employeeRepository.findAllById(employeeIds);
            if (employees.isEmpty()) {
                return "queuedCommands=0";
            }
            List<Terminal> terminals = resolveTerminalsForEmployees(employees, terminalSns);
            int total = 0;
            for (Employee employee : employees) {
                for (Terminal terminal : terminals) {
                    total += queueCommand(terminal, "DATA QUERY USERINFO PIN=" + sanitize(employee.getEmpCode()));
                }
            }
            return "queuedCommands=" + total;
        });
    }

    @Async("deviceActionExecutor")
    @Transactional
    public void resyncAll(Long jobId, List<String> terminalSns) {
        processJob(jobId, () -> {
            List<Employee> employees = employeeRepository.findAll();
            if (employees.isEmpty()) {
                return "queuedCommands=0";
            }

            List<Terminal> terminals;
            if (terminalSns != null && !terminalSns.isEmpty()) {
                terminals = terminalRepository.findBySnInOrderByIdAsc(terminalSns);
            } else {
                terminals = terminalRepository.findAll();
            }
            int total = queueSyncCommands(employees, terminals);
            return "queuedCommands=" + total;
        });
    }

    @Async("deviceActionExecutor")
    @Transactional
    public void remoteEnroll(Long jobId, Long employeeId, EmployeeRemoteEnrollRequest request) {
        processJob(jobId, () -> {
            Employee employee = findEmployee(employeeId);
            List<String> sns = request != null ? request.getTerminalSns() : null;
            List<Terminal> terminals = resolveTerminalsForEmployee(employee, sns);
            if (terminals.isEmpty()) {
                return "queuedCommands=0";
            }

            int bioType = request != null && request.getBioType() != null ? request.getBioType() : 1;
            int fingerId = request != null && request.getFingerId() != null ? request.getFingerId() : 0;
            int retry = request != null && request.getRetry() != null ? request.getRetry() : 3;
            int overwrite = request != null && request.getOverwrite() != null ? request.getOverwrite() : 1;
            String cardNo = request != null ? sanitize(request.getCardNo()) : "";

            String pin = sanitize(employee.getEmpCode());
            String command = buildRemoteEnrollCommand(bioType, pin, fingerId, retry, overwrite, cardNo);

            int total = 0;
            for (Terminal terminal : terminals) {
                total += queueCommand(terminal, command);
            }

            employee.setEnrollSn(terminals.get(0).getSn());
            employee.setUpdateTime(OffsetDateTime.now());
            employeeRepository.save(employee);
            return "queuedCommands=" + total;
        });
    }

    @Async("deviceActionExecutor")
    @Transactional
    public void deleteBiometricData(Long jobId, Long employeeId, List<Integer> bioTypes, List<String> terminalSns) {
        processJob(jobId, () -> {
            Employee employee = findEmployee(employeeId);
            List<Integer> deleteTypes = (bioTypes == null || bioTypes.isEmpty()) ? List.of(1, 2, 6, 8) : bioTypes;
            List<Terminal> terminals = resolveTerminalsForEmployee(employee, terminalSns);
            int total = 0;

            for (Terminal terminal : terminals) {
                for (Integer bioType : deleteTypes) {
                    total += queueDeleteCommands(terminal, employee, bioType);
                }
            }

            List<BioDataTemplate> templates = bioDataTemplateRepository.findByEmployeeIdAndBioTypeIn(employee.getId(), deleteTypes);
            bioDataTemplateRepository.deleteAll(templates);
            employee.setUpdateTime(OffsetDateTime.now());
            employeeRepository.save(employee);
            return "queuedCommands=" + total;
        });
    }

    @Async("deviceActionExecutor")
    @Transactional
    public void deleteEmployeeFromDevice(Long jobId, Long employeeId, List<Integer> bioTypes, List<String> terminalSns) {
        processJob(jobId, () -> {
            Employee employee = findEmployee(employeeId);
            List<Integer> deleteTypes = (bioTypes == null || bioTypes.isEmpty()) ? List.of(1, 2, 6, 8) : bioTypes;
            List<Terminal> terminals = resolveTerminalsForEmployee(employee, terminalSns);
            int total = 0;

            for (Terminal terminal : terminals) {
                total += queueCommand(terminal, buildDeleteEmployeeCommand(terminal, employee));
                for (Integer bioType : deleteTypes) {
                    total += queueDeleteCommands(terminal, employee, bioType);
                }
            }

            employee.setUpdateTime(OffsetDateTime.now());
            employeeRepository.save(employee);
            return "queuedCommands=" + total;
        });
    }

    private int queueSyncCommands(List<Employee> employees, List<Terminal> terminals) {
        int total = 0;
        for (Employee employee : employees) {
            for (Terminal terminal : terminals) {
                total += queueCommand(terminal, buildEmployeeUploadCommand(employee, terminal));
            }
        }
        return total;
    }

    private int queueCommand(Terminal terminal, String content) {
        TerminalCommand cmd = new TerminalCommand();
        cmd.setTerminal(terminal);
        cmd.setContent(content);
        cmd.setCommitTime(OffsetDateTime.now());
        terminalCommandRepository.save(cmd);
        return 1;
    }

    private String buildEmployeeUploadCommand(Employee employee, Terminal terminal) {
        int privilege = employee.getDevPrivilege() != null ? employee.getDevPrivilege() : 0;
        int verifyMode = employee.getVerifyMode() != null ? employee.getVerifyMode() : 0;
        String pin = sanitize(employee.getEmpCode());
        StringBuilder command = new StringBuilder();
        if (supportsUpdateUserInfo(terminal)) {
            command.append("DATA UPDATE USERINFO PIN=");
        } else {
            command.append("DATA USER PIN=");
        }
        command.append(pin)
                .append("\tName=").append(sanitize(employee.getFirstName()))
                .append("\tPasswd=").append(sanitize(employee.getDevicePassword()))
                .append("\tCard=").append(sanitize(employee.getCardNo()))
                .append("\tPri=").append(privilege)
                .append("\tVerify=").append(verifyMode);

        appendTemplates(command, terminal, employee, pin, 1, terminal.getFpAlgVer());
        appendTemplates(command, terminal, employee, pin, 2, terminal.getFaceAlgVer());
        appendTemplates(command, terminal, employee, pin, 6, terminal.getFvAlgVer());
        appendTemplates(command, terminal, employee, pin, 8, terminal.getPalmAlgVer());
        return command.toString();
    }

    private String buildRemoteEnrollCommand(int bioType, String pin, int fingerId, int retry, int overwrite, String cardNo) {
        return switch (bioType) {
            case 0 -> "ENROLL_MF PIN=" + pin + "\tRETRY=" + retry;
            case 1 -> "ENROLL_FP PIN=" + pin + "\tFID=" + fingerId + "\tRETRY=" + retry + "\tOVERWRITE=" + overwrite;
            case 2, 6, 8 -> "ENROLL_BIO TYPE=" + bioType + "\tPIN=" + pin + "\tCardNo=" + sanitize(cardNo)
                    + "\tRETRY=" + retry + "\tOVERWRITE=" + overwrite;
            default -> throw new IllegalArgumentException("Unsupported bioType for remote enroll: " + bioType);
        };
    }

    private void appendTemplates(
            StringBuilder command,
            Terminal terminal,
            Employee employee,
            String pin,
            int bioType,
            String terminalAlgVer
    ) {
        List<BioDataTemplate> templates = bioDataTemplateRepository.findByEmployeeIdAndBioTypeOrderByBioNoAscBioIndexAsc(employee.getId(), bioType);
        if (templates.isEmpty()) {
            return;
        }

        for (BioDataTemplate template : templates) {
            command.append('\n').append(buildTemplateCommand(terminal, pin, bioType, terminalAlgVer, template));
        }
    }

    private String buildTemplateCommand(
            Terminal terminal,
            String pin,
            int bioType,
            String terminalAlgVer,
            BioDataTemplate template
    ) {
        if (shouldUseBioData(terminalAlgVer, template.getMajorVer(), bioType)) {
            return "DATA UPDATE BIODATA Pin=" + pin
                    + "\tNo=" + safeInt(template.getBioNo())
                    + "\tIndex=" + safeInt(template.getBioIndex())
                    + "\tValid=" + safeInt(template.getValid(), 1)
                    + "\tDuress=" + safeInt(template.getDuress(), 0)
                    + "\tType=" + bioType
                    + "\tMajorVer=" + sanitize(template.getMajorVer())
                    + "\tMinorVer=" + sanitize(template.getMinorVer())
                    + "\tTmp=" + sanitize(template.getBioTmp());
        }

        if (bioType == 1) {
            if (supportsUpdateUserInfo(terminal)) {
                return "DATA UPDATE FINGERTMP PIN=" + pin
                        + "\tFID=" + safeInt(template.getBioNo())
                        + "\tSize=" + sanitize(Integer.toString(template.getBioTmp() != null ? template.getBioTmp().length() : 0))
                        + "\tTMP=" + sanitize(template.getBioTmp())
                        + "\tValid=" + safeInt(template.getValid(), 1);
            }
            return "DATA FP PIN=" + pin
                    + "\tFID=" + safeInt(template.getBioNo())
                    + "\tTMP=" + sanitize(template.getBioTmp())
                    + "\tValid=" + safeInt(template.getValid(), 1);
        }

        if (bioType == 2) {
            return "DATA UPDATE FACE PIN=" + pin
                    + "\tFID=" + safeInt(template.getBioIndex())
                    + "\tSize=" + sanitize(Integer.toString(template.getBioTmp() != null ? template.getBioTmp().length() : 0))
                    + "\tValid=" + safeInt(template.getValid(), 1)
                    + "\tTMP=" + sanitize(template.getBioTmp());
        }

        return "DATA UPDATE BIODATA Pin=" + pin
                + "\tNo=" + safeInt(template.getBioNo())
                + "\tIndex=" + safeInt(template.getBioIndex())
                + "\tValid=" + safeInt(template.getValid(), 1)
                + "\tDuress=" + safeInt(template.getDuress(), 0)
                + "\tType=" + bioType
                + "\tMajorVer=" + sanitize(template.getMajorVer())
                + "\tMinorVer=" + sanitize(template.getMinorVer())
                + "\tTmp=" + sanitize(template.getBioTmp());
    }

    private int queueDeleteCommands(Terminal terminal, Employee employee, Integer bioType) {
        String pin = sanitize(employee.getEmpCode());
        if (bioType == null) {
            return 0;
        }

        if (bioType == 1) {
            List<BioDataTemplate> templates = bioDataTemplateRepository.findByEmployeeIdAndBioTypeOrderByBioNoAscBioIndexAsc(employee.getId(), 1);
            if (templates.isEmpty()) {
                return queueCommand(terminal, "DATA DELETE BIODATA Pin=" + pin + "\tType=1");
            }
            int total = 0;
            for (BioDataTemplate template : templates) {
                if (usesLegacyFingerprintDelete(terminal, template)) {
                    String command = supportsUpdateUserInfo(terminal)
                            ? "DATA DELETE FINGERTMP PIN=" + pin + "\tFID=" + safeInt(template.getBioNo())
                            : "DATA DEL_FP PIN=" + pin + "\tFID=" + safeInt(template.getBioNo());
                    total += queueCommand(terminal, command);
                } else {
                    total += queueCommand(terminal, "DATA DELETE BIODATA Pin=" + pin + "\tType=1");
                }
            }
            return total;
        }

        if (bioType == 2) {
            if (algVersion(terminal.getFaceAlgVer()) > 7) {
                return queueCommand(terminal, "DATA DELETE BIODATA Pin=" + pin + "\tType=2");
            }
            return queueCommand(terminal, "DATA DELETE FACE PIN=" + pin);
        }

        if (bioType == 8) {
            return queueCommand(terminal, "DATA DELETE BIODATA Pin=" + pin + "\tType=8");
        }

        if (bioType == 6) {
            return queueCommand(terminal, "DATA DELETE BIODATA Pin=" + pin + "\tType=6");
        }

        return 0;
    }

    private String buildDeleteEmployeeCommand(Terminal terminal, Employee employee) {
        String pin = sanitize(employee.getEmpCode());
        Short productType = terminal.getProductType();
        if (productType != null && (productType == 5 || productType == 15 || productType == 25)) {
            return "DATA DELETE user Pin=" + pin;
        }
        if (supportsUpdateUserInfo(terminal)) {
            return "DATA DELETE USERINFO PIN=" + pin;
        }
        return "DATA DEL_USER PIN=" + pin;
    }

    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + employeeId));
    }

    private List<Terminal> resolveTerminalsForEmployee(Employee employee, List<String> terminalSns) {
        if (terminalSns != null && !terminalSns.isEmpty()) {
            return terminalRepository.findBySnInOrderByIdAsc(terminalSns);
        }
        Set<Long> areaIds = employee.getAreas().stream().map(Area::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        if (!areaIds.isEmpty()) {
            return terminalRepository.findDistinctByAreaIdInOrderByIdAsc(areaIds);
        }
        return terminalRepository.findAll();
    }

    private List<Terminal> resolveTerminalsForEmployees(Collection<Employee> employees, List<String> terminalSns) {
        if (terminalSns != null && !terminalSns.isEmpty()) {
            return terminalRepository.findBySnInOrderByIdAsc(terminalSns);
        }
        Set<Long> areaIds = new LinkedHashSet<>();
        for (Employee employee : employees) {
            for (Area area : employee.getAreas()) {
                areaIds.add(area.getId());
            }
        }
        if (!areaIds.isEmpty()) {
            return terminalRepository.findDistinctByAreaIdInOrderByIdAsc(areaIds);
        }
        return terminalRepository.findAll();
    }

    private void processJob(Long jobId, JobRunner runner) {
        asyncJobService.markRunning(jobId);
        try {
            String result = runner.run();
            asyncJobService.markSuccess(jobId, result);
        } catch (Exception ex) {
            asyncJobService.markFailed(jobId, ex.getMessage());
        }
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim();
    }

    private boolean supportsUpdateUserInfo(Terminal terminal) {
        String pushProtocol = terminal.getPushProtocol();
        return pushProtocol != null && pushProtocol.contains("2.2.14");
    }

    private boolean shouldUseBioData(String terminalAlgVer, String templateMajorVer, int bioType) {
        if (bioType == 6 || bioType == 8) {
            return true;
        }
        return algVersion(terminalAlgVer) > (bioType == 1 ? 10 : 8) && algVersion(templateMajorVer) >= 10;
    }

    private boolean usesLegacyFingerprintDelete(Terminal terminal, BioDataTemplate template) {
        return algVersion(template.getMajorVer()) <= 10 && algVersion(terminal.getFpAlgVer()) <= 10;
    }

    private int algVersion(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return (int) Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String safeInt(Integer value) {
        return Integer.toString(value != null ? value : 0);
    }

    private String safeInt(Integer value, int defaultValue) {
        return Integer.toString(value != null ? value : defaultValue);
    }

    @FunctionalInterface
    private interface JobRunner {
        String run();
    }
}
