package com.gth.posting.application.dto;

import java.time.Instant;
import java.util.List;

public record CampaignDto(
        String id,
        String name,
        String productName,
        long priceAmount,
        String priceFormatted,
        String warrantyText,
        String phoneNumber,
        String contentObjective,
        String brandName,
        List<String> photoPaths,
        String status,
        Instant createdAt
) {
}
