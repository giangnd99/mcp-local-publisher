package com.gth.posting.application.usecase.createcampaign;

/**
 * Port vao cua use case "tao campaign".
 * Tang REST chi phu thuoc interface nay, khong biet service hien thuc la ai.
 */
public interface CreateCampaignUseCase {

    CreateCampaignResult handle(CreateCampaignCommand command);
}
