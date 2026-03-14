package Zkteco.zkteco.modules.iclock.dto;

import java.time.OffsetDateTime;

public record AttendanceReuploadCommandResponse(
        Long commandId,
        Long terminalId,
        String terminalSn,
        String terminalAlias,
        String command,
        boolean allData,
        String startTime,
        String endTime,
        OffsetDateTime commitTime
) {
}
