package Zkteco.zkteco.modules.employee.controller;

import Zkteco.zkteco.modules.employee.dto.EmployeeBatchDeviceActionRequest;
import Zkteco.zkteco.modules.employee.dto.EmployeeDeleteBioRequest;
import Zkteco.zkteco.modules.employee.dto.EmployeeDeleteFromDeviceRequest;
import Zkteco.zkteco.modules.employee.dto.EmployeeDeviceActionRequest;
import Zkteco.zkteco.modules.employee.service.EmployeeService;
import Zkteco.zkteco.modules.employee.service.EmployeeDeviceActionService;
import Zkteco.zkteco.modules.employee.dto.EmployeeResponse;
import Zkteco.zkteco.modules.employee.dto.EmployeeResyncAllRequest;
import Zkteco.zkteco.modules.employee.dto.EmployeeRemoteEnrollRequest;
import Zkteco.zkteco.modules.employee.dto.EmployeeUpsertRequest;
import Zkteco.zkteco.modules.job.dto.AsyncJobResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeDeviceActionService employeeDeviceActionService;

    public EmployeeController(
            EmployeeService employeeService,
            EmployeeDeviceActionService employeeDeviceActionService
    ) {
        this.employeeService = employeeService;
        this.employeeDeviceActionService = employeeDeviceActionService;
    }

    @GetMapping
    public List<EmployeeResponse> list(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String q
    ) {
        return employeeService.list(companyId, q);
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable Long id) {
        return employeeService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody EmployeeUpsertRequest request) {
        return employeeService.create(request);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeUpsertRequest request) {
        return employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        employeeService.delete(id);
    }

    @PostMapping("/{id}/actions/sync-to-device")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AsyncJobResponse syncEmployeeToDevice(
            @PathVariable Long id,
            @RequestBody(required = false) EmployeeDeviceActionRequest request
    ) {
        return employeeDeviceActionService.submitSyncEmployee(id, request);
    }

    @PostMapping("/{id}/actions/pull-from-device")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AsyncJobResponse pullEmployeeFromDevice(
            @PathVariable Long id,
            @RequestBody(required = false) EmployeeDeviceActionRequest request
    ) {
        return employeeDeviceActionService.submitPullEmployee(id, request);
    }

    @PostMapping("/actions/sync-batch-to-device")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AsyncJobResponse syncBatchToDevice(@RequestBody(required = false) EmployeeBatchDeviceActionRequest request) {
        return employeeDeviceActionService.submitBatchSync(request);
    }

    @PostMapping("/actions/pull-batch-from-device")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AsyncJobResponse pullBatchFromDevice(@RequestBody(required = false) EmployeeBatchDeviceActionRequest request) {
        return employeeDeviceActionService.submitBatchPull(request);
    }

    @PostMapping("/actions/resync-all")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AsyncJobResponse resyncAll(@RequestBody(required = false) EmployeeResyncAllRequest request) {
        return employeeDeviceActionService.submitResyncAll(request);
    }

    @PostMapping("/{id}/actions/remote-enroll")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AsyncJobResponse remoteEnroll(
            @PathVariable Long id,
            @RequestBody(required = false) EmployeeRemoteEnrollRequest request
    ) {
        return employeeDeviceActionService.submitRemoteEnroll(id, request);
    }

    @PostMapping("/{id}/actions/remote-enroll-fingerprint")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AsyncJobResponse remoteEnrollFingerprint(
            @PathVariable Long id,
            @RequestBody(required = false) EmployeeRemoteEnrollRequest request
    ) {
        return employeeDeviceActionService.submitRemoteEnrollFingerprint(id, request);
    }

    @PostMapping("/{id}/actions/delete-biometrics")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AsyncJobResponse deleteBiometrics(
            @PathVariable Long id,
            @RequestBody(required = false) EmployeeDeleteBioRequest request
    ) {
        return employeeDeviceActionService.submitDeleteBiometrics(id, request);
    }

    @PostMapping("/{id}/actions/delete-from-device")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AsyncJobResponse deleteFromDevice(
            @PathVariable Long id,
            @RequestBody(required = false) EmployeeDeleteFromDeviceRequest request
    ) {
        return employeeDeviceActionService.submitDeleteFromDevice(id, request);
    }
}
