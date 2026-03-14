package Zkteco.zkteco.modules.iclock.dto;

import jakarta.validation.constraints.NotBlank;

public class TerminalCommandCreateRequest {

    @NotBlank
    private String sn;

    @NotBlank
    private String content;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
