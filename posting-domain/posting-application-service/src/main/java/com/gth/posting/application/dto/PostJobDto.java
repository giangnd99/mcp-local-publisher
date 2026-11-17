package com.gth.posting.application.dto;

import java.time.Instant;

public record PostJobDto(
        String id,
        String campaignId,
        String groupId,
        String groupName,
        String status,
        int attempt,
        Instant scheduledAt,
        Instant lastAttemptAt,
        Instant postedAt,
        String fbPostUrl,
        String errorMessage,
        String preview
) {
}
