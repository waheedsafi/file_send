package Zkteco.zkteco.modules.iclock.dto;

import java.time.OffsetDateTime;

public record TerminalCommandResponse(
        Long id,
        String terminalSn,
        String content,
        OffsetDateTime commitTime,
        OffsetDateTime transferTime,
        OffsetDateTime returnTime,
        Integer returnValue
) {
}
