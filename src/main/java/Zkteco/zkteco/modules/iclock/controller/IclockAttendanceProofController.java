package Zkteco.zkteco.modules.iclock.controller;

import Zkteco.zkteco.modules.iclock.dto.AttendanceProofCommandResponse;
import Zkteco.zkteco.modules.iclock.dto.AttendanceProofRequest;
import Zkteco.zkteco.modules.iclock.dto.AttendanceProofResponse;
import Zkteco.zkteco.modules.iclock.service.IclockAttendanceProofService;
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
@RequestMapping("/api/v1/iclock/attendance-proof")
public class IclockAttendanceProofController {

    private final IclockAttendanceProofService attendanceProofService;

    public IclockAttendanceProofController(IclockAttendanceProofService attendanceProofService) {
        this.attendanceProofService = attendanceProofService;
    }

    @PostMapping("/commands")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceProofCommandResponse createCommand(
            @RequestBody(required = false) AttendanceProofRequest requestBody,
            @RequestParam Map<String, String> params
    ) {
        return attendanceProofService.queueAttendanceProof(mergeRequest(requestBody, params));
    }

    @GetMapping("/proofs")
    public List<AttendanceProofResponse> listProofs(
            @RequestParam(required = false) String sn,
            @RequestParam(required = false) Integer limit
    ) {
        return attendanceProofService.listProofs(sn, limit);
    }

    private AttendanceProofRequest mergeRequest(
            AttendanceProofRequest requestBody,
            Map<String, String> params
    ) {
        AttendanceProofRequest request = requestBody != null ? requestBody : new AttendanceProofRequest();
        if ((request.getSn() == null || request.getSn().isBlank()) && params.containsKey("sn")) {
            request.setSn(params.get("sn"));
        }
        if ((request.getStartTime() == null || request.getStartTime().isBlank()) && params.containsKey("startTime")) {
            request.setStartTime(params.get("startTime"));
        }
        if ((request.getEndTime() == null || request.getEndTime().isBlank()) && params.containsKey("endTime")) {
            request.setEndTime(params.get("endTime"));
        }
        if (request.getSn() == null || request.getSn().isBlank()) {
            throw new BadRequestException("sn is required");
        }
        if (request.getStartTime() == null || request.getStartTime().isBlank()) {
            throw new BadRequestException("startTime is required");
        }
        if (request.getEndTime() == null || request.getEndTime().isBlank()) {
            throw new BadRequestException("endTime is required");
        }
        return request;
    }
}
