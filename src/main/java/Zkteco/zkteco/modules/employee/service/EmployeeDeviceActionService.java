package Zkteco.zkteco.modules.employee.service;

import Zkteco.zkteco.modules.employee.dto.EmployeeBatchDeviceActionRequest;
import Zkteco.zkteco.modules.employee.dto.EmployeeDeleteBioRequest;
import Zkteco.zkteco.modules.employee.dto.EmployeeDeleteFromDeviceRequest;
import Zkteco.zkteco.modules.employee.dto.EmployeeDeviceActionRequest;
import Zkteco.zkteco.modules.employee.dto.EmployeeResyncAllRequest;
import Zkteco.zkteco.modules.employee.dto.EmployeeRemoteEnrollRequest;
import Zkteco.zkteco.modules.job.dto.AsyncJobResponse;
import Zkteco.zkteco.modules.job.service.AsyncJobService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EmployeeDeviceActionService {

    private final AsyncJobService asyncJobService;
    private final EmployeeDeviceActionWorker employeeDeviceActionWorker;

    public EmployeeDeviceActionService(
            AsyncJobService asyncJobService,
            EmployeeDeviceActionWorker employeeDeviceActionWorker
    ) {
        this.asyncJobService = asyncJobService;
        this.employeeDeviceActionWorker = employeeDeviceActionWorker;
    }

    public AsyncJobResponse submitSyncEmployee(Long employeeId, EmployeeDeviceActionRequest request) {
        List<String> sns = request != null ? request.getTerminalSns() : null;
        AsyncJobResponse job = asyncJobService.create("SYNC_EMPLOYEE_TO_DEVICE", "employeeId=" + employeeId);
        employeeDeviceActionWorker.syncSingleEmployee(job.id(), employeeId, sns);
        return job;
    }

    public AsyncJobResponse submitPullEmployee(Long employeeId, EmployeeDeviceActionRequest request) {
        List<String> sns = request != null ? request.getTerminalSns() : null;
        AsyncJobResponse job = asyncJobService.create("PULL_EMPLOYEE_FROM_DEVICE", "employeeId=" + employeeId);
        employeeDeviceActionWorker.pullSingleEmployee(job.id(), employeeId, sns);
        return job;
    }

    public AsyncJobResponse submitBatchSync(EmployeeBatchDeviceActionRequest request) {
        AsyncJobResponse job = asyncJobService.create("SYNC_EMPLOYEE_BATCH", "batch");
        employeeDeviceActionWorker.syncEmployeeBatch(job.id(), request != null ? request.getEmployeeIds() : null,
                request != null ? request.getTerminalSns() : null);
        return job;
    }

    public AsyncJobResponse submitBatchPull(EmployeeBatchDeviceActionRequest request) {
        AsyncJobResponse job = asyncJobService.create("PULL_EMPLOYEE_BATCH", "batch");
        employeeDeviceActionWorker.pullEmployeeBatch(job.id(), request != null ? request.getEmployeeIds() : null,
                request != null ? request.getTerminalSns() : null);
        return job;
    }

    public AsyncJobResponse submitResyncAll(EmployeeResyncAllRequest request) {
        AsyncJobResponse job = asyncJobService.create("RESYNC_ALL_EMPLOYEES", "all");
        employeeDeviceActionWorker.resyncAll(job.id(), request != null ? request.getTerminalSns() : null);
        return job;
    }

    public AsyncJobResponse submitRemoteEnroll(Long employeeId, EmployeeRemoteEnrollRequest request) {
        AsyncJobResponse job = asyncJobService.create("REMOTE_ENROLL_EMPLOYEE", "employeeId=" + employeeId);
        employeeDeviceActionWorker.remoteEnroll(job.id(), employeeId, request);
        return job;
    }

    public AsyncJobResponse submitRemoteEnrollFingerprint(Long employeeId, EmployeeRemoteEnrollRequest request) {
        if (request == null) {
            request = new EmployeeRemoteEnrollRequest();
        }
        if (request.getBioType() == null) {
            request.setBioType(1);
        }
        AsyncJobResponse job = asyncJobService.create("REMOTE_ENROLL_FINGERPRINT", "employeeId=" + employeeId);
        employeeDeviceActionWorker.remoteEnroll(job.id(), employeeId, request);
        return job;
    }

    public AsyncJobResponse submitDeleteBiometrics(Long employeeId, EmployeeDeleteBioRequest request) {
        AsyncJobResponse job = asyncJobService.create("DELETE_EMPLOYEE_BIOMETRICS", "employeeId=" + employeeId);
        employeeDeviceActionWorker.deleteBiometricData(job.id(), employeeId,
                request != null ? request.getBioTypes() : null,
                request != null ? request.getTerminalSns() : null);
        return job;
    }

    public AsyncJobResponse submitDeleteFromDevice(Long employeeId, EmployeeDeleteFromDeviceRequest request) {
        AsyncJobResponse job = asyncJobService.create("DELETE_EMPLOYEE_FROM_DEVICE", "employeeId=" + employeeId);
        employeeDeviceActionWorker.deleteEmployeeFromDevice(job.id(), employeeId,
                request != null ? request.getBioTypes() : null,
                request != null ? request.getTerminalSns() : null);
        return job;
    }
}
