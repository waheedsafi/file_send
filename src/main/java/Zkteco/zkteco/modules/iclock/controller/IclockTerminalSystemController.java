package Zkteco.zkteco.modules.iclock.controller;

import Zkteco.zkteco.modules.iclock.dto.TerminalBatchCommandResponse;
import Zkteco.zkteco.modules.iclock.dto.TerminalBooleanOptionRequest;
import Zkteco.zkteco.modules.iclock.dto.TerminalDateTimeSyncRequest;
import Zkteco.zkteco.modules.iclock.dto.TerminalDaylightSavingTimeRequest;
import Zkteco.zkteco.modules.iclock.dto.TerminalFilePullRequest;
import Zkteco.zkteco.modules.iclock.dto.TerminalIntegerOptionRequest;
import Zkteco.zkteco.modules.iclock.dto.TerminalResponse;
import Zkteco.zkteco.modules.iclock.dto.TerminalSetOptionRequest;
import Zkteco.zkteco.modules.iclock.service.IclockTerminalSystemCommandService;
import Zkteco.zkteco.web.error.BadRequestException;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iclock/terminals/{sn}/system")
public class IclockTerminalSystemController {

    private final IclockTerminalSystemCommandService terminalSystemCommandService;

    public IclockTerminalSystemController(IclockTerminalSystemCommandService terminalSystemCommandService) {
        this.terminalSystemCommandService = terminalSystemCommandService;
    }

    @PostMapping("/info")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalBatchCommandResponse readInfo(@PathVariable String sn) {
        return terminalSystemCommandService.queueReadInfo(sn);
    }

    @PostMapping("/reboot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalBatchCommandResponse reboot(@PathVariable String sn) {
        return terminalSystemCommandService.queueReboot(sn);
    }

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalBatchCommandResponse reset(@PathVariable String sn) {
        return terminalSystemCommandService.queueReset(sn);
    }

    @PostMapping("/check-all")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalBatchCommandResponse checkAll(@PathVariable String sn) {
        return terminalSystemCommandService.queueCheckAll(sn);
    }

    @PostMapping("/clear-log")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalBatchCommandResponse clearLog(@PathVariable String sn) {
        return terminalSystemCommandService.queueClearLog(sn);
    }

    @PostMapping("/clear-data")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalBatchCommandResponse clearData(@PathVariable String sn) {
        return terminalSystemCommandService.queueClearData(sn);
    }

    @PostMapping("/set-option")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalBatchCommandResponse setOption(
            @PathVariable String sn,
            @RequestBody(required = false) TerminalSetOptionRequest requestBody,
            @RequestParam Map<String, String> params
    ) {
        TerminalSetOptionRequest request = requestBody != null ? requestBody : new TerminalSetOptionRequest();
        if ((request.getOption() == null || request.getOption().isBlank()) && params.containsKey("option")) {
            request.setOption(params.get("option"));
        }
        if ((request.getValue() == null || request.getValue().isBlank()) && params.containsKey("value")) {
            request.setValue(params.get("value"));
        }
        return terminalSystemCommandService.queueSetOption(sn, requiredText(request.getOption(), "option"), requiredText(request.getValue(), "value"));
    }

    @PostMapping("/sync-time")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalBatchCommandResponse syncTime(
            @PathVariable String sn,
            @RequestBody(required = false) TerminalDateTimeSyncRequest requestBody,
            @RequestParam Map<String, String> params
    ) {
        TerminalDateTimeSyncRequest request = requestBody != null ? requestBody : new TerminalDateTimeSyncRequest();
        if ((request.getDateTime() == null || request.getDateTime().isBlank()) && params.containsKey("dateTime")) {
            request.setDateTime(params.get("dateTime"));
        }
        return terminalSystemCommandService.queueSyncTime(sn, request);
    }

    @PostMapping("/require-punch-state")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalBatchCommandResponse requirePunchState(
            @PathVariable String sn,
            @RequestBody(required = false) TerminalBooleanOptionRequest requestBody,
            @RequestParam Map<String, String> params
    ) {
        TerminalBooleanOptionRequest request = mergeBooleanRequest(requestBody, params);
        boolean enabled = request != null && Boolean.TRUE.equals(request.getEnabled());
        return terminalSystemCommandService.queueRequirePunchState(sn, enabled);
    }

    @PostMapping("/duplicate-punch-period")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalBatchCommandResponse duplicatePunchPeriod(
            @PathVariable String sn,
            @RequestBody(required = false) TerminalIntegerOptionRequest requestBody,
            @RequestParam Map<String, String> params
    ) {
        TerminalIntegerOptionRequest request = mergeIntegerRequest(requestBody, params, "value");
        return terminalSystemCommandService.queueDuplicatePunchPeriod(sn, request.getValue());
    }

    @PostMapping("/capture-setting")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalBatchCommandResponse captureSetting(
            @PathVariable String sn,
            @RequestBody(required = false) TerminalIntegerOptionRequest requestBody,
            @RequestParam Map<String, String> params
    ) {
        TerminalIntegerOptionRequest request = mergeIntegerRequest(requestBody, params, "value");
        return terminalSystemCommandService.queueCaptureSetting(sn, request.getValue());
    }

    @PostMapping("/capture-upload")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalBatchCommandResponse captureUpload(
            @PathVariable String sn,
            @RequestBody(required = false) TerminalBooleanOptionRequest requestBody,
            @RequestParam Map<String, String> params
    ) {
        TerminalBooleanOptionRequest request = mergeBooleanRequest(requestBody, params);
        boolean enabled = request != null && Boolean.TRUE.equals(request.getEnabled());
        return terminalSystemCommandService.queueCaptureUpload(sn, enabled);
    }

    @PostMapping("/daylight-saving-time")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalBatchCommandResponse daylightSavingTime(
            @PathVariable String sn,
            @RequestBody TerminalDaylightSavingTimeRequest request
    ) {
        return terminalSystemCommandService.queueDaylightSavingTime(sn, request);
    }

    @PostMapping("/pull-file")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalBatchCommandResponse pullFile(
            @PathVariable String sn,
            @RequestBody(required = false) TerminalFilePullRequest requestBody,
            @RequestParam Map<String, String> params
    ) {
        TerminalFilePullRequest request = requestBody != null ? requestBody : new TerminalFilePullRequest();
        if ((request.getFilePath() == null || request.getFilePath().isBlank()) && params.containsKey("filePath")) {
            request.setFilePath(params.get("filePath"));
        }
        return terminalSystemCommandService.queuePullFile(sn, request.getFilePath());
    }

    @PostMapping("/disable")
    public TerminalResponse disable(@PathVariable String sn) {
        return terminalSystemCommandService.disableTerminal(sn);
    }

    @PostMapping("/enable")
    public TerminalResponse enable(@PathVariable String sn) {
        return terminalSystemCommandService.enableTerminal(sn);
    }

    private TerminalIntegerOptionRequest mergeIntegerRequest(
            TerminalIntegerOptionRequest requestBody,
            Map<String, String> params,
            String paramName
    ) {
        TerminalIntegerOptionRequest request = requestBody != null ? requestBody : new TerminalIntegerOptionRequest();
        if (request.getValue() == null && params.containsKey(paramName)) {
            try {
                request.setValue(Integer.parseInt(params.get(paramName).trim()));
            } catch (NumberFormatException ex) {
                throw new BadRequestException(paramName + " must be an integer");
            }
        }
        if (request.getValue() == null) {
            throw new BadRequestException(paramName + " is required");
        }
        return request;
    }

    private TerminalBooleanOptionRequest mergeBooleanRequest(
            TerminalBooleanOptionRequest requestBody,
            Map<String, String> params
    ) {
        TerminalBooleanOptionRequest request = requestBody != null ? requestBody : new TerminalBooleanOptionRequest();
        if (request.getEnabled() == null && params.containsKey("enabled")) {
            request.setEnabled(Boolean.parseBoolean(params.get("enabled")));
        }
        return request;
    }

    private String requiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " is required");
        }
        return value;
    }
}
