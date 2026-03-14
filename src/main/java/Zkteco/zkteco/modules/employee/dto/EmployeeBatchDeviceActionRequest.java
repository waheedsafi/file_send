package Zkteco.zkteco.modules.employee.dto;

import java.util.List;

public class EmployeeBatchDeviceActionRequest {

    private List<Long> employeeIds;
    private List<String> terminalSns;
    private List<Integer> bioTypes;

    public List<Long> getEmployeeIds() {
        return employeeIds;
    }

    public void setEmployeeIds(List<Long> employeeIds) {
        this.employeeIds = employeeIds;
    }

    public List<String> getTerminalSns() {
        return terminalSns;
    }

    public void setTerminalSns(List<String> terminalSns) {
        this.terminalSns = terminalSns;
    }

    public List<Integer> getBioTypes() {
        return bioTypes;
    }

    public void setBioTypes(List<Integer> bioTypes) {
        this.bioTypes = bioTypes;
    }
}
