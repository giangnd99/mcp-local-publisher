package com.gth.posting.domain.entity;

/**
 * Vong doi cua campaign (mot dot: 1 san pham x N nhom).
 */
public enum CampaignStatus {

    DRAFT,
    ACTIVE,
    PAUSED,
    COMPLETED,
    ARCHIVED;

    public boolean acceptsNewJobs() {
        return this == DRAFT || this == ACTIVE;
    }

    public boolean canDispatch() {
        return this == ACTIVE;
    }
}
