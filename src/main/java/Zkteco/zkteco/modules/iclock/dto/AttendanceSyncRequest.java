package Zkteco.zkteco.modules.iclock.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AttendanceSyncRequest {

    @NotBlank
    private String sn;

    private String payload;

    private List<String> lines;

    private String event;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
