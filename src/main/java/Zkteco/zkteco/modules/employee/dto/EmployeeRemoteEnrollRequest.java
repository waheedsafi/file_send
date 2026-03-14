package Zkteco.zkteco.modules.employee.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

public class EmployeeRemoteEnrollRequest {

    private List<String> terminalSns;

    /**
     * Bio type aligned with BioTime/iClock commands:
     * 0=MF card, 1=fingerprint, 2=face, 6=finger vein, 8=palm.
     */
    @Min(0)
    @Max(8)
    private Integer bioType = 1;

    @Min(0)
    @Max(9)
    private Integer fingerId = 0;

    @Min(1)
    @Max(10)
    private Integer retry = 3;

    @Min(0)
    @Max(1)
    private Integer overwrite = 1;

    private String cardNo;

    public List<String> getTerminalSns() {
        return terminalSns;
    }

    public void setTerminalSns(List<String> terminalSns) {
        this.terminalSns = terminalSns;
    }

    public Integer getBioType() {
        return bioType;
    }

    public void setBioType(Integer bioType) {
        this.bioType = bioType;
    }

    public Integer getFingerId() {
        return fingerId;
    }

    public void setFingerId(Integer fingerId) {
        this.fingerId = fingerId;
    }

    public Integer getRetry() {
        return retry;
    }

    public void setRetry(Integer retry) {
        this.retry = retry;
    }

    public Integer getOverwrite() {
        return overwrite;
    }

    public void setOverwrite(Integer overwrite) {
        this.overwrite = overwrite;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }
}
