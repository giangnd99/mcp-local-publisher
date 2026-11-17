package com.gth.posting.application.usecase.validatecampaign;

import java.util.List;

public record ValidateCampaignResult(
        String campaignId,
        boolean allValid,
        int validatedCount,
        int failedCount,
        List<JobIssue> issues
) {

    public ValidateCampaignResult {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }

    public record JobIssue(String postJobId, String groupName, List<String> messages) {
        public JobIssue {
            messages = messages == null ? List.of() : List.copyOf(messages);
        }
    }
}
