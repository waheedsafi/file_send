package Zkteco.zkteco.modules.iclock.controller;

import Zkteco.zkteco.modules.iclock.service.IclockProtocolService;
import Zkteco.zkteco.modules.iclock.dto.TerminalCommandCreateRequest;
import Zkteco.zkteco.modules.iclock.dto.TerminalCommandResponse;
import Zkteco.zkteco.modules.iclock.dto.AttendanceTransactionResponse;
import Zkteco.zkteco.modules.iclock.dto.TerminalResponse;
import Zkteco.zkteco.modules.iclock.dto.TerminalSetOptionRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iclock")
public class IclockManagementController {

    private final IclockProtocolService protocolService;

    public IclockManagementController(IclockProtocolService protocolService) {
        this.protocolService = protocolService;
    }

    @GetMapping("/devices")
    public List<TerminalResponse> listDevices() {
        return protocolService.listTerminals();
    }

    @GetMapping("/terminals")
    public List<TerminalResponse> listTerminals() {
        return protocolService.listTerminals();
    }

    @GetMapping("/terminals/{sn}/commands")
    public List<TerminalCommandResponse> listCommands(@PathVariable String sn) {
        return protocolService.listCommands(sn);
    }

    @PostMapping("/commands")
    @ResponseStatus(HttpStatus.CREATED)
    public TerminalCommandResponse createCommand(@Valid @RequestBody TerminalCommandCreateRequest request) {
        return protocolService.queueCommand(request.getSn(), request.getContent());
    }

    @GetMapping("/attendance/recent")
    public List<AttendanceTransactionResponse> recentAttendance(
            @RequestParam(required = false) String terminalSn,
            @RequestParam(required = false) String empCode,
            @RequestParam(required = false) Integer limit
    ) {
        return protocolService.listRecentAttendance(terminalSn, empCode, limit);
    }

    @PostMapping("/terminals/{sn}/actions/reboot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalCommandResponse reboot(@PathVariable String sn) {
        return protocolService.queueReboot(sn);
    }

    @PostMapping("/terminals/{sn}/actions/read-info")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalCommandResponse readInfo(@PathVariable String sn) {
        return protocolService.queueReadInfo(sn);
    }

    @PostMapping("/terminals/{sn}/actions/check-all")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalCommandResponse checkAll(@PathVariable String sn) {
        return protocolService.queueCheckAll(sn);
    }

    @PostMapping("/terminals/{sn}/actions/clear-data")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalCommandResponse clearData(@PathVariable String sn) {
        return protocolService.queueClearData(sn);
    }

    @PostMapping("/terminals/{sn}/actions/clear-log")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalCommandResponse clearLog(@PathVariable String sn) {
        return protocolService.queueClearLog(sn);
    }

    @PostMapping("/terminals/{sn}/actions/set-option")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TerminalCommandResponse setOption(@PathVariable String sn, @Valid @RequestBody TerminalSetOptionRequest request) {
        return protocolService.queueSetOption(sn, request.getOption(), request.getValue());
    }

    @PostMapping("/terminals/{sn}/actions/disable")
    public TerminalResponse disable(@PathVariable String sn) {
        return protocolService.setTerminalEnabled(sn, false);
    }

    @PostMapping("/terminals/{sn}/actions/enable")
    public TerminalResponse enable(@PathVariable String sn) {
        return protocolService.setTerminalEnabled(sn, true);
    }
}
