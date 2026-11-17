package com.gth.posting.application.mapper;

import com.gth.posting.application.dto.CampaignDto;
import com.gth.posting.domain.entity.Campaign;

public final class CampaignDtoMapper {

    private CampaignDtoMapper() {
    }

    /** Chuyển chiến dịch miền thành dữ liệu đọc cho giao diện và agent MCP. */
    public static CampaignDto toDto(Campaign campaign) {
        return new CampaignDto(
                campaign.id().value(),
                campaign.name(),
                campaign.productName(),
                campaign.price().amount(),
                campaign.price().formatted(),
                campaign.warrantyText(),
                campaign.phoneNumber(),
                campaign.contentObjective().name(),
                campaign.brandName(),
                campaign.photoPaths(),
                campaign.status().name(),
                campaign.createdAt()
        );
    }
}
