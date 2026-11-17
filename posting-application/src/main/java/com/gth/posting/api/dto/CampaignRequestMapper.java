package com.gth.posting.api.dto;

import com.gth.posting.application.usecase.createcampaign.CreateCampaignCommand;
import com.gth.posting.domain.valueobject.ContentObjective;

import java.util.List;

public final class CampaignRequestMapper {

    private CampaignRequestMapper() {
    }

    /** Chuyển yêu cầu HTTP thành lệnh nghiệp vụ và áp dụng mục tiêu mặc định an toàn. */
    public static CreateCampaignCommand toCommand(CreateCampaignRequest request) {
        List<CreateCampaignCommand.GroupInput> groups = request.groups() == null
                ? List.of()
                : request.groups().stream()
                .map(item -> new CreateCampaignCommand.GroupInput(item.groupId(), item.name(),
                        item.memberCount(), item.region(), item.text()))
                .toList();

        return new CreateCampaignCommand(
                request.name(),
                request.productName(),
                request.priceAmount() == null ? 0 : request.priceAmount(),
                request.warrantyText(),
                request.phoneNumber(),
                ContentObjective.from(request.contentObjective()),
                request.productFacts(),
                request.brandName(),
                request.brandPromise(),
                request.photoPaths(),
                groups
        );
    }
}
