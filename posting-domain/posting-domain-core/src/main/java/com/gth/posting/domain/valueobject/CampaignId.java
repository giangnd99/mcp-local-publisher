package com.gth.posting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifier cua mot campaign (mot dot dang bai cho mot san pham).
 * Pure Java: khong dung annotation cua framework nao.
 */
public record CampaignId(String value) {

    public CampaignId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CampaignId khong duoc rong");
        }
    }

    public static CampaignId of(String value) {
        return new CampaignId(value);
    }

    public static CampaignId newId() {
        return new CampaignId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return Objects.toString(value);
    }
}
