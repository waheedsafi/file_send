package Zkteco.zkteco.modules.iclock.dto;

import java.time.OffsetDateTime;

public record AttendanceProofResponse(
        Long id,
        Long terminalId,
        String terminalSn,
        String terminalAlias,
        OffsetDateTime actionTime,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        Integer terminalCount,
        Integer serverCount,
        Short flag
) {
}
