package com.gth.posting.domain.event;

import com.gth.posting.domain.entity.PostStatus;
import com.gth.posting.domain.valueobject.PostJobId;

import java.time.Instant;

public record PostJobStateChanged(
        PostJobId postJobId,
        PostStatus from,
        PostStatus to,
        String reason,
        Instant occurredAt
) implements DomainEvent {
}
