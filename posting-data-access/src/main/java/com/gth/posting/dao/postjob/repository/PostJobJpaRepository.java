package com.gth.posting.dao.postjob.repository;

import com.gth.posting.dao.postjob.entity.PostJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PostJobJpaRepository extends JpaRepository<PostJobEntity, String> {

    List<PostJobEntity> findByCampaignIdOrderByCreatedAtAsc(String campaignId);

    List<PostJobEntity> findByCampaignIdAndStatus(String campaignId, String status);

    List<PostJobEntity> findByStatusOrderByCreatedAtAsc(String status);

    @Query("""
            select j from PostJobEntity j
            where j.campaignId = :campaignId
              and j.status = 'QUEUED'
              and j.scheduledAt <= :now
            order by j.scheduledAt asc
            """)
    List<PostJobEntity> findDue(@Param("campaignId") String campaignId, @Param("now") Instant now);

    @Query("select max(j.postedAt) from PostJobEntity j where j.postedAt is not null")
    Optional<Instant> findLastPostedAt();

    long countByCampaignIdAndStatus(String campaignId, String status);

    boolean existsByCampaignIdAndGroupId(String campaignId, String groupId);
}
