package Zkteco.zkteco.modules.iclock.dto;

import java.time.OffsetDateTime;

public record TerminalResponse(
        Long id,
        String sn,
        String alias,
        String ipAddress,
        String realIp,
        Short productType,
        Short isAttendance,
        Short isRegistration,
        OffsetDateTime lastActivity,
        Long areaId,
        String areaName
) {
}
