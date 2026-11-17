package com.gth.posting.application.usecase.createcampaign;

import com.gth.posting.application.dto.CampaignDto;
import com.gth.posting.application.dto.PostJobDto;

import java.util.List;

public record CreateCampaignResult(CampaignDto campaign, List<PostJobDto> jobs, List<String> warnings) {

    public CreateCampaignResult {
        jobs = jobs == null ? List.of() : List.copyOf(jobs);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
