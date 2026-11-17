package com.gth.posting.domain.entity;

import com.gth.posting.domain.valueobject.PostJobId;

import java.time.Instant;
import java.util.Objects;

/**
 * Nhat ky bat bien cua tung job. Dung de tra loi cau hoi
 * "vi sao bai nay khong len?" ma khong phai doc log server.
 */
public record JobEvent(
        String id,
        PostJobId postJobId,
        JobEventType type,
        String payload,
        Instant createdAt
) {

    public JobEvent {
        Objects.requireNonNull(postJobId, "JobEvent can postJobId");
        Objects.requireNonNull(type, "JobEvent can type");
        payload = payload == null ? "" : payload;
    }

    public static JobEvent of(PostJobId postJobId, JobEventType type, String payload, Instant now) {
        return new JobEvent(java.util.UUID.randomUUID().toString(), postJobId, type, payload, now);
    }
}
