package Zkteco.zkteco.modules.employee.dto;

import java.util.List;

public class EmployeeDeviceActionRequest {

    private List<String> terminalSns;

    public List<String> getTerminalSns() {
        return terminalSns;
    }

    public void setTerminalSns(List<String> terminalSns) {
        this.terminalSns = terminalSns;
    }
}
