package Zkteco.zkteco.modules.iclock.service;

import Zkteco.zkteco.domain.iclock.Terminal;
import Zkteco.zkteco.domain.iclock.TerminalCommand;
import Zkteco.zkteco.modules.iclock.dto.AttendanceReuploadCommandResponse;
import Zkteco.zkteco.modules.iclock.dto.AttendanceReuploadRequest;
import Zkteco.zkteco.repository.iclock.TerminalCommandRepository;
import Zkteco.zkteco.repository.iclock.TerminalRepository;
import Zkteco.zkteco.web.error.BadRequestException;
import Zkteco.zkteco.web.error.NotFoundException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IclockAttendanceReuploadService {

    private static final DateTimeFormatter COMMAND_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TerminalRepository terminalRepository;
    private final TerminalCommandRepository terminalCommandRepository;

    public IclockAttendanceReuploadService(
            TerminalRepository terminalRepository,
            TerminalCommandRepository terminalCommandRepository
    ) {
        this.terminalRepository = terminalRepository;
        this.terminalCommandRepository = terminalCommandRepository;
    }

    public AttendanceReuploadCommandResponse queueAttendanceReupload(AttendanceReuploadRequest request) {
        Terminal terminal = terminalRepository.findBySn(request.getSn().trim())
                .orElseThrow(() -> new NotFoundException("Terminal not found: " + request.getSn()));

        HistoricalCommand historicalCommand = buildHistoricalCommand(terminal, request);

        TerminalCommand command = new TerminalCommand();
        command.setTerminal(terminal);
        command.setContent(historicalCommand.command());
        command.setCommitTime(OffsetDateTime.now());

        TerminalCommand saved = terminalCommandRepository.save(command);
        return new AttendanceReuploadCommandResponse(
                saved.getId(),
                terminal.getId(),
                terminal.getSn(),
                terminal.getAlias(),
                saved.getContent(),
                historicalCommand.allData(),
                historicalCommand.startTime(),
                historicalCommand.endTime(),
                saved.getCommitTime()
        );
    }

    @Transactional(readOnly = true)
    public List<AttendanceReuploadCommandResponse> listAttendanceReuploadCommands(String sn, Integer limit) {
        int max = limit == null ? 50 : Math.max(1, Math.min(limit, 200));
        List<TerminalCommand> commands;

        if (sn == null || sn.isBlank()) {
            commands = terminalCommandRepository.findAll();
        } else {
            Terminal terminal = terminalRepository.findBySn(sn.trim())
                    .orElseThrow(() -> new NotFoundException("Terminal not found: " + sn));
            commands = terminalCommandRepository.findByTerminalIdOrderByIdAsc(terminal.getId());
        }

        return commands.stream()
                .filter(this::isAttendanceReuploadCommand)
                .sorted(Comparator.comparing(TerminalCommand::getId).reversed())
                .limit(max)
                .map(this::mapCommand)
                .toList();
    }

    private HistoricalCommand buildHistoricalCommand(Terminal terminal, AttendanceReuploadRequest request) {
        if (request.isAllData()) {
            if (isAccessPanel(terminal)) {
                return new HistoricalCommand(
                        "DATA QUERY tablename=transaction,fielddesc=*,filter=*",
                        true,
                        null,
                        null
                );
            }
            terminal.setLogStamp("0");
            terminalRepository.save(terminal);
            return new HistoricalCommand("CHECK LOG", true, null, null);
        }

        LocalDateTime start = parseRequiredCommandTime(request.getStartTime(), "startTime");
        LocalDateTime end = parseOptionalCommandTime(request.getEndTime(), LocalDateTime.now(ZoneOffset.UTC));
        if (end.isBefore(start)) {
            throw new BadRequestException("endTime must be greater than or equal to startTime");
        }

        String startText = start.format(COMMAND_TIME_FORMAT);
        String endText = end.format(COMMAND_TIME_FORMAT);
        String command = "DATA QUERY ATTLOG StartTime=" + startText + "\tEndTime=" + endText;
        return new HistoricalCommand(command, false, startText, endText);
    }

    private boolean isAttendanceReuploadCommand(TerminalCommand command) {
        String content = command.getContent();
        if (content == null) {
            return false;
        }
        return content.startsWith("DATA QUERY ATTLOG ")
                || content.equals("CHECK LOG")
                || content.equals("DATA QUERY tablename=transaction,fielddesc=*,filter=*");
    }

    private AttendanceReuploadCommandResponse mapCommand(TerminalCommand command) {
        String content = command.getContent();
        boolean allData = !content.startsWith("DATA QUERY ATTLOG ");
        String startTime = null;
        String endTime = null;

        if (content.startsWith("DATA QUERY ATTLOG ")) {
            startTime = extractRangeValue(content, "StartTime=");
            endTime = extractRangeValue(content, "EndTime=");
        }

        return new AttendanceReuploadCommandResponse(
                command.getId(),
                command.getTerminal().getId(),
                command.getTerminal().getSn(),
                command.getTerminal().getAlias(),
                content,
                allData,
                startTime,
                endTime,
                command.getCommitTime()
        );
    }

    private String extractRangeValue(String command, String prefix) {
        int start = command.indexOf(prefix);
        if (start < 0) {
            return null;
        }
        start += prefix.length();
        int end = command.indexOf('\t', start);
        if (end < 0) {
            end = command.length();
        }
        return command.substring(start, end).trim();
    }

    private boolean isAccessPanel(Terminal terminal) {
        Short productType = terminal.getProductType();
        return productType != null && (productType == 5 || productType == 15 || productType == 25);
    }

    private LocalDateTime parseRequiredCommandTime(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " is required when allData is false");
        }
        return parseCommandTime(value, fieldName);
    }

    private LocalDateTime parseOptionalCommandTime(String value, LocalDateTime defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return parseCommandTime(value, "endTime");
    }

    private LocalDateTime parseCommandTime(String value, String fieldName) {
        try {
            return LocalDateTime.parse(value.trim(), COMMAND_TIME_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException(fieldName + " must use format yyyy-MM-dd HH:mm:ss");
        }
    }

    private record HistoricalCommand(String command, boolean allData, String startTime, String endTime) {
    }
}
