package com.gth.posting.dao.postjob.adapter;

import com.gth.posting.application.port.out.repository.PostJobRepository;
import com.gth.posting.dao.postjob.mapper.PostJobDataAccessMapper;
import com.gth.posting.dao.postjob.repository.PostJobJpaRepository;
import com.gth.posting.domain.entity.PostJob;
import com.gth.posting.domain.entity.PostStatus;
import com.gth.posting.domain.valueobject.CampaignId;
import com.gth.posting.domain.valueobject.GroupId;
import com.gth.posting.domain.valueobject.PostJobId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class PostJobRepositoryImpl implements PostJobRepository {

    private final PostJobJpaRepository jpaRepository;

    public PostJobRepositoryImpl(PostJobJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PostJob save(PostJob job) {
        return PostJobDataAccessMapper.toDomain(
                jpaRepository.save(PostJobDataAccessMapper.toEntity(job)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostJob> findById(PostJobId id) {
        return jpaRepository.findById(id.value()).map(PostJobDataAccessMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostJob> findByCampaignId(CampaignId campaignId) {
        return jpaRepository.findByCampaignIdOrderByCreatedAtAsc(campaignId.value())
                .stream().map(PostJobDataAccessMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostJob> findByCampaignIdAndStatus(CampaignId campaignId, PostStatus status) {
        return jpaRepository.findByCampaignIdAndStatus(campaignId.value(), status.name())
                .stream().map(PostJobDataAccessMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostJob> findNextQueued(CampaignId campaignId, Instant now) {
        return jpaRepository.findDue(campaignId.value(), now).stream()
                .findFirst()
                .map(PostJobDataAccessMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostJob> findByStatus(PostStatus status) {
        return jpaRepository.findByStatusOrderByCreatedAtAsc(status.name())
                .stream().map(PostJobDataAccessMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Instant> findLastPostedAt() {
        return jpaRepository.findLastPostedAt();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCampaignIdAndStatus(CampaignId campaignId, PostStatus status) {
        return jpaRepository.countByCampaignIdAndStatus(campaignId.value(), status.name());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCampaignIdAndGroupId(CampaignId campaignId, GroupId groupId) {
        return jpaRepository.existsByCampaignIdAndGroupId(campaignId.value(), groupId.value());
    }
}
