package com.gth.posting.dao.postjob.mapper;

import com.gth.posting.dao.postjob.entity.PostJobEntity;
import com.gth.posting.domain.entity.PostJob;
import com.gth.posting.domain.entity.PostStatus;
import com.gth.posting.domain.valueobject.CampaignId;
import com.gth.posting.domain.valueobject.GroupId;
import com.gth.posting.domain.valueobject.PostJobId;
import com.gth.posting.domain.valueobject.PostText;

public final class PostJobDataAccessMapper {

    private PostJobDataAccessMapper() {
    }

    public static PostJobEntity toEntity(PostJob job) {
        return new PostJobEntity(
                job.id().value(),
                job.campaignId().value(),
                job.groupId().value(),
                job.groupName(),
                job.text().content(),
                job.status().name(),
                job.attempt(),
                job.scheduledAt(),
                job.lastAttemptAt(),
                job.postedAt(),
                job.fbPostUrl(),
                job.errorMessage(),
                job.createdAt(),
                job.updatedAt()
        );
    }

    public static PostJob toDomain(PostJobEntity entity) {
        return PostJob.restore(
                PostJobId.of(entity.getId()),
                CampaignId.of(entity.getCampaignId()),
                GroupId.of(entity.getGroupId()),
                entity.getGroupName(),
                PostText.of(entity.getContent()),
                PostStatus.valueOf(entity.getStatus()),
                entity.getAttempt(),
                entity.getScheduledAt(),
                entity.getLastAttemptAt(),
                entity.getPostedAt(),
                entity.getFbPostUrl(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
