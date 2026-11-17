package com.gth.posting.application.usecase.getcampaign;

import com.gth.posting.application.dto.CampaignDto;
import com.gth.posting.application.dto.GroupDto;
import com.gth.posting.application.dto.PostJobDto;

import java.util.List;

public record GetCampaignResult(CampaignDto campaign, List<PostJobDto> jobs, List<GroupDto> groups) {

    public GetCampaignResult {
        jobs = jobs == null ? List.of() : List.copyOf(jobs);
        groups = groups == null ? List.of() : List.copyOf(groups);
    }
}
