package Zkteco.zkteco.modules.iclock.dto;

import jakarta.validation.constraints.NotBlank;

public class AttendanceProofRequest {

    @NotBlank
    private String sn;

    @NotBlank
    private String startTime;

    @NotBlank
    private String endTime;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
