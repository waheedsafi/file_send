package Zkteco.zkteco.modules.iclock.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AttendanceTransactionResponse(
        Long id,
        String terminalSn,
        String terminalAlias,
        String areaAlias,
        String empCode,
        Long employeeId,
        String employeeFirstName,
        String employeeLastName,
        OffsetDateTime punchTime,
        String punchState,
        Integer verifyType,
        String workCode,
        Double longitude,
        Double latitude,
        Short isMask,
        BigDecimal temperature,
        OffsetDateTime uploadTime
) {
}
