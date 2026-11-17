package com.gth.posting.dao.campaign.mapper;

import com.gth.posting.dao.campaign.entity.CampaignEntity;
import com.gth.posting.domain.entity.Campaign;
import com.gth.posting.domain.entity.CampaignStatus;
import com.gth.posting.domain.valueobject.CampaignId;
import com.gth.posting.domain.valueobject.ContentObjective;
import com.gth.posting.domain.valueobject.Money;

import java.util.Arrays;
import java.util.List;

/**
 * Chuyen doi giua domain model va JPA entity.
 * Domain khong bao gio nhin thay CampaignEntity.
 */
public final class CampaignDataAccessMapper {

    private static final String SEPARATOR = "|";

    private CampaignDataAccessMapper() {
    }

    public static CampaignEntity toEntity(Campaign campaign) {
        return new CampaignEntity(
                campaign.id().value(),
                campaign.name(),
                campaign.productName(),
                campaign.price().amount(),
                campaign.warrantyText(),
                campaign.phoneNumber(),
                campaign.contentObjective().name(),
                campaign.brandName(),
                campaign.brandNameRequired(),
                String.join(SEPARATOR, campaign.photoPaths()),
                campaign.status().name(),
                campaign.createdAt(),
                campaign.updatedAt()
        );
    }

    public static Campaign toDomain(CampaignEntity entity) {
        return Campaign.restore(
                CampaignId.of(entity.getId()),
                entity.getName(),
                entity.getProductName(),
                Money.vnd(entity.getPriceAmount()),
                entity.getWarrantyText(),
                entity.getPhoneNumber(),
                ContentObjective.from(entity.getContentObjective()),
                entity.getBrandName(),
                entity.isBrandNameRequired(),
                toPhotoList(entity.getPhotoPaths()),
                CampaignStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /** Tách danh sách ảnh đã lưu thành các đường dẫn độc lập. */
    private static List<String> toPhotoList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split("\\" + SEPARATOR))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
