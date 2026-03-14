package Zkteco.zkteco.modules.employee.dto;

import java.util.List;

public class EmployeeDeleteBioRequest {

    private List<String> terminalSns;
    private List<Integer> bioTypes;

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
