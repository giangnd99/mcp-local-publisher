package com.gth.posting.application.port.out.repository;

import com.gth.posting.domain.entity.PostJob;
import com.gth.posting.domain.entity.PostStatus;
import com.gth.posting.domain.valueobject.CampaignId;
import com.gth.posting.domain.valueobject.GroupId;
import com.gth.posting.domain.valueobject.PostJobId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PostJobRepository {

    PostJob save(PostJob job);

    Optional<PostJob> findById(PostJobId id);

    List<PostJob> findByCampaignId(CampaignId campaignId);

    List<PostJob> findByCampaignIdAndStatus(CampaignId campaignId, PostStatus status);

    /** Bai QUEUED den han, cu nhat truoc. */
    Optional<PostJob> findNextQueued(CampaignId campaignId, Instant now);

    List<PostJob> findByStatus(PostStatus status);

    /** Thoi diem dang thanh cong gan nhat tren toan he thong (de tinh nhip). */
    Optional<Instant> findLastPostedAt();

    long countByCampaignIdAndStatus(CampaignId campaignId, PostStatus status);

    boolean existsByCampaignIdAndGroupId(CampaignId campaignId, GroupId groupId);
}
