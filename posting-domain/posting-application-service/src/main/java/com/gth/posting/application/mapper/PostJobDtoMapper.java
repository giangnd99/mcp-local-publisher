package com.gth.posting.application.mapper;

import com.gth.posting.application.dto.PostJobDto;
import com.gth.posting.domain.entity.PostJob;

public final class PostJobDtoMapper {

    private static final int PREVIEW_LENGTH = 90;

    private PostJobDtoMapper() {
    }

    public static PostJobDto toDto(PostJob job) {
        String text = job.text() == null ? "" : job.text().content();
        String preview = text.length() > PREVIEW_LENGTH ? text.substring(0, PREVIEW_LENGTH) + "..." : text;
        return new PostJobDto(
                job.id().value(),
                job.campaignId().value(),
                job.groupId().value(),
                job.groupName(),
                job.status().name(),
                job.attempt(),
                job.scheduledAt(),
                job.lastAttemptAt(),
                job.postedAt(),
                job.fbPostUrl(),
                job.errorMessage(),
                preview
        );
    }
}
