package Zkteco.zkteco.modules.iclock.dto;

import java.time.OffsetDateTime;

public record AttendanceProofCommandResponse(
        Long commandId,
        Long terminalId,
        String terminalSn,
        String terminalAlias,
        String command,
        String startTime,
        String endTime,
        OffsetDateTime commitTime
) {
}
