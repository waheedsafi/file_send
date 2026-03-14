package Zkteco.zkteco.modules.iclock.dto;

import java.time.OffsetDateTime;

public record AttendanceSyncResponse(
        Long terminalId,
        String terminalSn,
        String terminalAlias,
        int receivedCount,
        int savedCount,
        int duplicateCount,
        int errorCount,
        OffsetDateTime uploadTime
) {
}
