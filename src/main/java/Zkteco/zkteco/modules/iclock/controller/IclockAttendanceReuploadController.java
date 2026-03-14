package Zkteco.zkteco.modules.iclock.controller;

import Zkteco.zkteco.modules.iclock.dto.AttendanceReuploadCommandResponse;
import Zkteco.zkteco.modules.iclock.dto.AttendanceReuploadRequest;
import Zkteco.zkteco.modules.iclock.service.IclockAttendanceReuploadService;
import Zkteco.zkteco.web.error.BadRequestException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iclock/attendance-reupload")
public class IclockAttendanceReuploadController {

    private final IclockAttendanceReuploadService attendanceReuploadService;

    public IclockAttendanceReuploadController(IclockAttendanceReuploadService attendanceReuploadService) {
        this.attendanceReuploadService = attendanceReuploadService;
    }

    @PostMapping("/commands")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceReuploadCommandResponse createCommand(
            @RequestBody(required = false) AttendanceReuploadRequest requestBody,
            @RequestParam Map<String, String> params
    ) {
        return attendanceReuploadService.queueAttendanceReupload(mergeRequest(requestBody, params));
    }

    @GetMapping("/commands")
    public List<AttendanceReuploadCommandResponse> listCommands(
            @RequestParam(required = false) String sn,
            @RequestParam(required = false) Integer limit
    ) {
        return attendanceReuploadService.listAttendanceReuploadCommands(sn, limit);
    }

    private AttendanceReuploadRequest mergeRequest(
            AttendanceReuploadRequest requestBody,
            Map<String, String> params
    ) {
        AttendanceReuploadRequest request = requestBody != null ? requestBody : new AttendanceReuploadRequest();

        if ((request.getSn() == null || request.getSn().isBlank()) && params.containsKey("sn")) {
            request.setSn(params.get("sn"));
        }
        if (request.getSn() == null || request.getSn().isBlank()) {
            throw new BadRequestException("sn is required");
        }

        if (request.getStartTime() == null && params.containsKey("startTime")) {
            request.setStartTime(params.get("startTime"));
        }
        if (request.getEndTime() == null && params.containsKey("endTime")) {
            request.setEndTime(params.get("endTime"));
        }
        if (params.containsKey("allData")) {
            request.setAllData(Boolean.parseBoolean(params.get("allData")));
        }

        return request;
    }
}
