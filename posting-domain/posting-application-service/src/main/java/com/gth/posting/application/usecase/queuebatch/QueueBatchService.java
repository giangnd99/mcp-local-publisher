package com.gth.posting.application.usecase.queuebatch;

import com.gth.posting.application.port.out.clock.TimeProvider;
import com.gth.posting.application.port.out.repository.CampaignRepository;
import com.gth.posting.application.port.out.repository.GroupRepository;
import com.gth.posting.application.port.out.repository.PostJobRepository;
import com.gth.posting.application.support.JobEventRecorder;
import com.gth.posting.domain.entity.Campaign;
import com.gth.posting.domain.entity.JobEventType;
import com.gth.posting.domain.entity.PostJob;
import com.gth.posting.domain.exception.PostingDomainException;
import com.gth.posting.domain.service.BatchSelectionPolicy;
import com.gth.posting.domain.service.GroupPostingPolicy;
import com.gth.posting.domain.service.SchedulingPolicy;
import com.gth.posting.domain.valueobject.CampaignId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class QueueBatchService implements QueueBatchUseCase {

    private static final int MAX_BATCH_SIZE = 20;

    private final CampaignRepository campaignRepository;
    private final PostJobRepository postJobRepository;
    private final GroupRepository groupRepository;
    private final JobEventRecorder eventRecorder;
    private final SchedulingPolicy schedulingPolicy;
    private final BatchSelectionPolicy batchSelectionPolicy;
    private final TimeProvider timeProvider;

    public QueueBatchService(CampaignRepository campaignRepository,
                             PostJobRepository postJobRepository,
                             GroupRepository groupRepository,
                             JobEventRecorder eventRecorder,
                             SchedulingPolicy schedulingPolicy,
                             BatchSelectionPolicy batchSelectionPolicy,
                             TimeProvider timeProvider) {
        this.campaignRepository = campaignRepository;
        this.postJobRepository = postJobRepository;
        this.groupRepository = groupRepository;
        this.eventRecorder = eventRecorder;
        this.schedulingPolicy = schedulingPolicy;
        this.batchSelectionPolicy = batchSelectionPolicy;
        this.timeProvider = timeProvider;
    }

    @Override
    public QueueBatchResult handle(String campaignId, int batchSize) {
        if (batchSize <= 0 || batchSize > MAX_BATCH_SIZE) {
            throw new PostingDomainException("INVALID_BATCH_SIZE", "batchSize phai trong khoang 1.." + MAX_BATCH_SIZE);
        }
        CampaignId id = CampaignId.of(campaignId);
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new PostingDomainException("CAMPAIGN_NOT_FOUND", "Khong thay campaign " + campaignId));
        if (!campaign.status().canDispatch()) {
            throw new PostingDomainException("CAMPAIGN_NOT_ACTIVE",
                    "Campaign dang " + campaign.status() + ", khong the xep lo");
        }

        Instant now = timeProvider.now();
        List<PostJob> candidates = postJobRepository.findByCampaignId(id).stream()
                .filter(job -> groupRepository.findById(job.groupId())
                        .filter(GroupPostingPolicy::isAllowed)
                        .isPresent())
                .toList();
        List<PostJob> selected = batchSelectionPolicy.select(candidates, batchSize, now);
        Instant globalLastPostAt = postJobRepository.findLastPostedAt().orElse(null);

        Instant cursor = now;
        List<QueueBatchResult.Slot> slots = new ArrayList<>();
        for (PostJob job : selected) {
            Instant groupLastPostAt = groupRepository.findById(job.groupId())
                    .map(group -> group.lastPostAt())
                    .orElse(null);
            Instant slot = schedulingPolicy.nextAllowedAt(cursor, globalLastPostAt, groupLastPostAt);

            job.queue(now, slot);
            postJobRepository.save(job);
            eventRecorder.recordStateChanges(job);
            eventRecorder.record(job.id(), JobEventType.NOTE, "Xep lo, du kien " + slot, now);

            slots.add(new QueueBatchResult.Slot(job.id().value(), job.groupName(), slot));
            cursor = slot.plus(schedulingPolicy.globalGap());
        }

        return new QueueBatchResult(campaignId, slots.size(), slots);
    }
}
