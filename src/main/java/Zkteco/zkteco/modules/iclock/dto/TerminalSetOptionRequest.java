package Zkteco.zkteco.modules.iclock.dto;

import jakarta.validation.constraints.NotBlank;

public class TerminalSetOptionRequest {

    @NotBlank
    private String option;

    @NotBlank
    private String value;

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
