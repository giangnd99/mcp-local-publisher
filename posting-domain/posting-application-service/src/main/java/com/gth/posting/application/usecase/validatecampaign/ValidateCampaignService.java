package com.gth.posting.application.usecase.validatecampaign;

import com.gth.posting.application.port.out.clock.TimeProvider;
import com.gth.posting.application.port.out.repository.CampaignRepository;
import com.gth.posting.application.port.out.repository.PostJobRepository;
import com.gth.posting.application.support.JobEventRecorder;
import com.gth.posting.domain.entity.Campaign;
import com.gth.posting.domain.entity.JobEventType;
import com.gth.posting.domain.entity.PostJob;
import com.gth.posting.domain.entity.PostStatus;
import com.gth.posting.domain.exception.PostingDomainException;
import com.gth.posting.domain.service.ContentPolicy;
import com.gth.posting.domain.service.ContentValidationResult;
import com.gth.posting.domain.valueobject.CampaignId;
import com.gth.posting.domain.valueobject.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ValidateCampaignService implements ValidateCampaignUseCase {

    private final CampaignRepository campaignRepository;
    private final PostJobRepository postJobRepository;
    private final JobEventRecorder eventRecorder;
    private final ContentPolicy contentPolicy;
    private final TimeProvider timeProvider;

    public ValidateCampaignService(CampaignRepository campaignRepository,
                                   PostJobRepository postJobRepository,
                                   JobEventRecorder eventRecorder,
                                   ContentPolicy contentPolicy,
                                   TimeProvider timeProvider) {
        this.campaignRepository = campaignRepository;
        this.postJobRepository = postJobRepository;
        this.eventRecorder = eventRecorder;
        this.contentPolicy = contentPolicy;
        this.timeProvider = timeProvider;
    }

    @Override
    public ValidateCampaignResult handle(String campaignId) {
        CampaignId id = CampaignId.of(campaignId);
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new PostingDomainException("CAMPAIGN_NOT_FOUND", "Khong thay campaign " + campaignId));

        ContentPolicy.Requirement requirement = new ContentPolicy.Requirement(
                Money.vnd(campaign.price().amount()),
                campaign.warrantyText(),
                campaign.phoneNumber(),
                campaign.contentObjective(),
                campaign.brandName(),
                campaign.brandNameRequired()
        );

        Instant now = timeProvider.now();
        List<PostJob> jobs = postJobRepository.findByCampaignId(id);
        List<PostJob> drafts = jobs.stream().filter(job -> job.status() == PostStatus.DRAFT).toList();

        List<ContentValidationResult> results = new ArrayList<>();
        for (PostJob job : drafts) {
            results.add(contentPolicy.validate(job.text(), requirement));
        }
        List<com.gth.posting.domain.service.ContentViolation> uniqueness =
                contentPolicy.validateUniqueness(drafts.stream().map(PostJob::text).toList());

        int validated = 0;
        int failed = 0;
        List<ValidateCampaignResult.JobIssue> issues = new ArrayList<>();

        for (int i = 0; i < drafts.size(); i++) {
            PostJob job = drafts.get(i);
            ContentValidationResult result = results.get(i);
            if (result.valid()) {
                job.validate(now);
                failed += 0;
                validated++;
                postJobRepository.save(job);
                eventRecorder.recordStateChanges(job);
            } else {
                failed++;
                eventRecorder.record(job.id(), JobEventType.VALIDATION_FAILED, result.describe(), now);
                issues.add(new ValidateCampaignResult.JobIssue(job.id().value(), job.groupName(),
                        result.violations().stream().map(v -> v.code() + ": " + v.message()).toList()));
            }
        }

        if (!uniqueness.isEmpty()) {
            failed = drafts.size() - validated;
            ValidateCampaignResult.JobIssue duplicateIssue = new ValidateCampaignResult.JobIssue(
                    drafts.isEmpty() ? "-" : drafts.get(0).id().value(),
                    "toan campaign",
                    uniqueness.stream().map(v -> v.code() + ": " + v.message()).toList());
            issues.add(duplicateIssue);
        }

        return new ValidateCampaignResult(campaignId, issues.isEmpty(), validated, failed, issues);
    }
}
