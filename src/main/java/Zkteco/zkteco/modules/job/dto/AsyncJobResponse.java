package Zkteco.zkteco.modules.job.dto;

import java.time.OffsetDateTime;

public record AsyncJobResponse(
        Long id,
        String action,
        String status,
        String payload,
        String result,
        OffsetDateTime createdAt,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        String errorMessage
) {
}
