package Zkteco.zkteco.modules.iclock.service;

import Zkteco.zkteco.domain.iclock.Terminal;
import Zkteco.zkteco.domain.iclock.TerminalCommand;
import Zkteco.zkteco.modules.iclock.dto.TerminalBatchCommandResponse;
import Zkteco.zkteco.modules.iclock.dto.TerminalCommandResponse;
import Zkteco.zkteco.modules.iclock.dto.TerminalDateTimeSyncRequest;
import Zkteco.zkteco.modules.iclock.dto.TerminalDaylightSavingTimeRequest;
import Zkteco.zkteco.modules.iclock.dto.TerminalResponse;
import Zkteco.zkteco.repository.iclock.TerminalCommandRepository;
import Zkteco.zkteco.repository.iclock.TerminalRepository;
import Zkteco.zkteco.web.error.BadRequestException;
import Zkteco.zkteco.web.error.NotFoundException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IclockTerminalSystemCommandService {

    private static final DateTimeFormatter COMMAND_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DLST_MODE_DATE_TIME = 0;
    private static final int DLST_MODE_MONTH_WEEK = 1;

    private final TerminalRepository terminalRepository;
    private final TerminalCommandRepository terminalCommandRepository;
    private final IclockProtocolService protocolService;

    public IclockTerminalSystemCommandService(
            TerminalRepository terminalRepository,
            TerminalCommandRepository terminalCommandRepository,
            IclockProtocolService protocolService
    ) {
        this.terminalRepository = terminalRepository;
        this.terminalCommandRepository = terminalCommandRepository;
        this.protocolService = protocolService;
    }

    public TerminalBatchCommandResponse queueReadInfo(String sn) {
        Terminal terminal = findTerminal(sn);
        List<String> commands = new ArrayList<>();
        if (isAccessPanel(terminal)) {
            commands.add("DATA COUNT user");
            commands.add("DATA COUNT transaction");
            commands.add("GET OPTIONS IPAddress,FirmVer,~DeviceName,CommType");
            if (terminal.getFpAlgVer() != null && !terminal.getFpAlgVer().isBlank()) {
                commands.add("DATA COUNT templatev10");
            }
            if (terminal.getFaceAlgVer() != null && !terminal.getFaceAlgVer().isBlank() && "101".equals(terminal.getStyle())) {
                commands.add("DATA COUNT facev7");
            }
        } else {
            commands.add("INFO");
        }
        return queueCommands(terminal, commands);
    }

    public TerminalBatchCommandResponse queueReboot(String sn) {
        Terminal terminal = findTerminal(sn);
        return queueCommands(terminal, List.of(rebootCommand(terminal)));
    }

    public TerminalBatchCommandResponse queueReset(String sn) {
        return queueReboot(sn);
    }

    public TerminalBatchCommandResponse queueCheckAll(String sn) {
        Terminal terminal = findTerminal(sn);
        return queueCommands(terminal, List.of("CHECK ALL"));
    }

    public TerminalBatchCommandResponse queueClearLog(String sn) {
        Terminal terminal = findTerminal(sn);
        return queueCommands(terminal, List.of(clearLogCommand(terminal)));
    }

    public TerminalBatchCommandResponse queueClearData(String sn) {
        Terminal terminal = findTerminal(sn);
        List<String> commands = new ArrayList<>();
        if (isAccessPanel(terminal)) {
            commands.add("DATA DELETE transaction *");
            commands.add("DATA DELETE user *");
            commands.add("DATA DELETE userauthorize *");
            commands.add("DATA DELETE holiday *");
            if (terminal.getFpAlgVer() != null && !terminal.getFpAlgVer().isBlank()) {
                commands.add("DATA DELETE templatev10 *");
            }
            if (terminal.getFvAlgVer() != null && !terminal.getFvAlgVer().isBlank()) {
                commands.add("DATA DELETE fvtemplate *");
            }
            commands.add("SET OPTIONS AntiPassback=0");
            commands.add("SET OPTIONS InterLock=0");
        } else {
            commands.add("CLEAR DATA");
        }
        return queueCommands(terminal, commands);
    }

    public TerminalBatchCommandResponse queueSetOption(String sn, String option, String value) {
        if (option == null || option.isBlank()) {
            throw new BadRequestException("option is required");
        }
        Terminal terminal = findTerminal(sn);
        String safeOption = sanitize(option);
        String safeValue = sanitize(value);
        String prefix = isAccessPanel(terminal) ? "SET OPTIONS " : "SET OPTION ";
        return queueCommands(terminal, List.of(prefix + safeOption + "=" + safeValue));
    }

    public TerminalBatchCommandResponse queueRequirePunchState(String sn, boolean enabled) {
        return queueSetOption(sn, "~MustChoiceInOut", enabled ? "1" : "0");
    }

    public TerminalBatchCommandResponse queueDuplicatePunchPeriod(String sn, Integer value) {
        if (value == null || value < 0 || value > 1440) {
            throw new BadRequestException("value must be between 0 and 1440");
        }
        return queueSetOption(sn, "AlarmReRec", Integer.toString(value));
    }

    public TerminalBatchCommandResponse queueCaptureSetting(String sn, Integer value) {
        if (value == null || value < 0 || value > 4) {
            throw new BadRequestException("value must be between 0 and 4");
        }
        return queueSetOption(sn, "CapturePic", Integer.toString(value));
    }

    public TerminalBatchCommandResponse queueCaptureUpload(String sn, boolean enabled) {
        return queueSetOption(sn, "UploadPhoto", enabled ? "1" : "0");
    }

    public TerminalBatchCommandResponse queueSyncTime(String sn, TerminalDateTimeSyncRequest request) {
        Terminal terminal = findTerminal(sn);
        LocalDateTime target = request == null || request.getDateTime() == null || request.getDateTime().isBlank()
                ? LocalDateTime.now(ZoneOffset.UTC)
                : parseCommandTime(request.getDateTime(), "dateTime");
        String prefix = isAccessPanel(terminal) ? "SET OPTIONS " : "SET OPTION ";
        return queueCommands(terminal, List.of(prefix + "DateTime=" + target.format(COMMAND_TIME_FORMAT)));
    }

    public TerminalBatchCommandResponse queueDaylightSavingTime(String sn, TerminalDaylightSavingTimeRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        Terminal terminal = findTerminal(sn);
        List<String> commands = new ArrayList<>();
        boolean enabled = Boolean.TRUE.equals(request.getEnabled());
        commands.add(optionPrefix(terminal) + "DaylightSavingTimeOn=" + (enabled ? 1 : 0));
        if (!enabled) {
            return queueCommands(terminal, commands);
        }

        int mode = request.getMode() == null ? DLST_MODE_DATE_TIME : request.getMode();
        if (mode != DLST_MODE_DATE_TIME && mode != DLST_MODE_MONTH_WEEK) {
            throw new BadRequestException("mode must be 0 or 1");
        }
        commands.add(optionPrefix(terminal) + "DLSTMode=" + mode);

        if (mode == DLST_MODE_DATE_TIME) {
            LocalDateTime start = parseCommandTime(request.getStartDateTime(), "startDateTime");
            LocalDateTime end = parseCommandTime(request.getEndDateTime(), "endDateTime");
            int startValue = encodeDstDateTime(start);
            int endValue = encodeDstDateTime(end);
            commands.add(optionPrefix(terminal) + "DaylightSavingTime=" + startValue);
            commands.add(optionPrefix(terminal) + "StandardTime=" + endValue);
        } else {
            commands.add(optionPrefix(terminal) + "WeekOfMonth1=" + requiredInt(request.getStartMonth(), "startMonth"));
            commands.add(optionPrefix(terminal) + "WeekOfMonth2=" + requiredInt(request.getStartWeek(), "startWeek"));
            commands.add(optionPrefix(terminal) + "WeekOfMonth3=" + requiredInt(request.getStartWeekday(), "startWeekday"));
            commands.add(optionPrefix(terminal) + "WeekOfMonth4=" + requiredInt(request.getStartHour(), "startHour"));
            commands.add(optionPrefix(terminal) + "WeekOfMonth5=" + requiredInt(request.getStartMinute(), "startMinute"));
            commands.add(optionPrefix(terminal) + "WeekOfMonth6=" + requiredInt(request.getEndMonth(), "endMonth"));
            commands.add(optionPrefix(terminal) + "WeekOfMonth7=" + requiredInt(request.getEndWeek(), "endWeek"));
            commands.add(optionPrefix(terminal) + "WeekOfMonth8=" + requiredInt(request.getEndWeekday(), "endWeekday"));
            commands.add(optionPrefix(terminal) + "WeekOfMonth9=" + requiredInt(request.getEndHour(), "endHour"));
            commands.add(optionPrefix(terminal) + "WeekOfMonth10=" + requiredInt(request.getEndMinute(), "endMinute"));
        }

        return queueCommands(terminal, commands);
    }

    public TerminalBatchCommandResponse queuePullFile(String sn, String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new BadRequestException("filePath is required");
        }
        Terminal terminal = findTerminal(sn);
        return queueCommands(terminal, List.of("GetFile " + sanitize(filePath)));
    }

    public TerminalResponse disableTerminal(String sn) {
        return protocolService.setTerminalEnabled(sn, false);
    }

    public TerminalResponse enableTerminal(String sn) {
        return protocolService.setTerminalEnabled(sn, true);
    }

    private TerminalBatchCommandResponse queueCommands(Terminal terminal, List<String> commands) {
        List<TerminalCommandResponse> queued = new ArrayList<>();
        for (String content : commands) {
            if (content == null || content.isBlank()) {
                continue;
            }
            TerminalCommand cmd = new TerminalCommand();
            cmd.setTerminal(terminal);
            cmd.setContent(content.trim());
            cmd.setCommitTime(OffsetDateTime.now());
            TerminalCommand saved = terminalCommandRepository.save(cmd);
            queued.add(new TerminalCommandResponse(
                    saved.getId(),
                    terminal.getSn(),
                    saved.getContent(),
                    saved.getCommitTime(),
                    saved.getTransferTime(),
                    saved.getReturnTime(),
                    saved.getReturnValue()
            ));
        }
        return new TerminalBatchCommandResponse(terminal.getId(), terminal.getSn(), terminal.getAlias(), queued);
    }

    private Terminal findTerminal(String sn) {
        return terminalRepository.findBySn(sn)
                .orElseThrow(() -> new NotFoundException("Terminal not found: " + sn));
    }

    private boolean isAccessPanel(Terminal terminal) {
        Short productType = terminal.getProductType();
        return productType != null && (productType == 5 || productType == 15 || productType == 25);
    }

    private String rebootCommand(Terminal terminal) {
        return isAccessPanel(terminal) ? "CONTROL DEVICE 0300" : "REBOOT";
    }

    private String clearLogCommand(Terminal terminal) {
        return isAccessPanel(terminal) ? "DATA DELETE transaction *" : "CLEAR LOG";
    }

    private String optionPrefix(Terminal terminal) {
        return isAccessPanel(terminal) ? "SET OPTIONS " : "SET OPTION ";
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim();
    }

    private LocalDateTime parseCommandTime(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " is required");
        }
        try {
            return LocalDateTime.parse(value.trim(), COMMAND_TIME_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException(fieldName + " must use format yyyy-MM-dd HH:mm:ss");
        }
    }

    private int encodeDstDateTime(LocalDateTime value) {
        return value.getMonthValue() << 24
                | value.getDayOfMonth() << 16
                | value.getHour() << 8
                | value.getMinute();
    }

    private int requiredInt(Integer value, String fieldName) {
        if (value == null) {
            throw new BadRequestException(fieldName + " is required");
        }
        return value;
    }
}
