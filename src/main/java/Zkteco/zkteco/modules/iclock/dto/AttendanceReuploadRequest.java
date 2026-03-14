package Zkteco.zkteco.modules.iclock.dto;

import jakarta.validation.constraints.NotBlank;

public class AttendanceReuploadRequest {

    @NotBlank
    private String sn;

    private boolean allData;

    private String startTime;

    private String endTime;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public boolean isAllData() {
        return allData;
    }

    public void setAllData(boolean allData) {
        this.allData = allData;
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
