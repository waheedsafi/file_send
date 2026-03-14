package Zkteco.zkteco.modules.iclock.dto;

import java.time.OffsetDateTime;

public record TerminalUploadLogResponse(
        Long id,
        Long terminalId,
        String terminalSn,
        String event,
        String content,
        Integer uploadCount,
        Integer errorCount,
        OffsetDateTime uploadTime
) {
}
