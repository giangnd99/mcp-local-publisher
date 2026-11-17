package com.gth.posting.application.usecase.queuebatch;

import java.time.Instant;
import java.util.List;

public record QueueBatchResult(String campaignId, int queued, List<Slot> slots) {

    public QueueBatchResult {
        slots = slots == null ? List.of() : List.copyOf(slots);
    }

    public record Slot(String postJobId, String groupName, Instant scheduledAt) {
    }
}
