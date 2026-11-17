package com.gth.posting.domain.event;

import com.gth.posting.domain.valueobject.CampaignId;

import java.time.Instant;

public record CampaignCreated(
        CampaignId campaignId,
        String name,
        int groupCount,
        Instant occurredAt
) implements DomainEvent {
}
