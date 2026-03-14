package Zkteco.zkteco.modules.iclock.dto;

import jakarta.validation.constraints.NotBlank;

public class TerminalFilePullRequest {

    @NotBlank
    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
