package Zkteco.zkteco.modules.iclock.controller;

import Zkteco.zkteco.modules.iclock.dto.AttendanceSyncRequest;
import Zkteco.zkteco.modules.iclock.dto.AttendanceSyncResponse;
import Zkteco.zkteco.modules.iclock.dto.TerminalUploadLogResponse;
import Zkteco.zkteco.modules.iclock.service.IclockAttendanceSyncService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iclock/attendance")
public class IclockAttendanceSyncController {

    private final IclockAttendanceSyncService attendanceSyncService;

    public IclockAttendanceSyncController(IclockAttendanceSyncService attendanceSyncService) {
        this.attendanceSyncService = attendanceSyncService;
    }

    @PostMapping("/sync")
    public AttendanceSyncResponse syncAttendance(@Valid @RequestBody AttendanceSyncRequest request) {
        return attendanceSyncService.syncFromManagement(request);
    }

    @GetMapping("/upload-logs")
    public List<TerminalUploadLogResponse> listUploadLogs(
            @RequestParam(required = false) String terminalSn,
            @RequestParam(required = false) Integer limit
    ) {
        return attendanceSyncService.listUploadLogs(terminalSn, limit);
    }
}
