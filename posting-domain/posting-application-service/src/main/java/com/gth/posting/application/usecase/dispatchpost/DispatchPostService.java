package com.gth.posting.application.usecase.dispatchpost;

import com.gth.posting.application.port.out.clock.TimeProvider;
import com.gth.posting.application.port.out.publisher.PostPublisher;
import com.gth.posting.application.port.out.publisher.PublishRequest;
import com.gth.posting.application.port.out.publisher.PublishResult;
import com.gth.posting.application.port.out.repository.CampaignRepository;
import com.gth.posting.application.port.out.repository.GroupRepository;
import com.gth.posting.application.port.out.repository.PostJobRepository;
import com.gth.posting.application.support.JobEventRecorder;
import com.gth.posting.domain.entity.Campaign;
import com.gth.posting.domain.entity.FbGroup;
import com.gth.posting.domain.entity.JobEventType;
import com.gth.posting.domain.entity.PostJob;
import com.gth.posting.domain.entity.PostStatus;
import com.gth.posting.domain.exception.PostingDomainException;
import com.gth.posting.domain.service.GroupPostingPolicy;
import com.gth.posting.domain.service.SchedulingPolicy;
import com.gth.posting.domain.valueobject.CampaignId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Dang mot bai den han.
 *
 * LUU Y: co tinh KHONG dat @Transactional o day. Ben trong co mot loi goi
 * trinh duyet keo dai 30-60 giay; giu transaction DB trong suot thoi gian do
 * se giu ket noi va khoa dong. Moi buoc ghi job duoc commit rieng, nho vay
 * neu chet giua duong van biet job dang o trang thai nao.
 */
@Service
public class DispatchPostService implements DispatchPostUseCase {

    private final CampaignRepository campaignRepository;
    private final PostJobRepository postJobRepository;
    private final GroupRepository groupRepository;
    private final JobEventRecorder eventRecorder;
    private final SchedulingPolicy schedulingPolicy;
    private final PostPublisher postPublisher;
    private final TimeProvider timeProvider;
    private final AutomationPropertiesView automationProperties;

    public DispatchPostService(CampaignRepository campaignRepository,
                               PostJobRepository postJobRepository,
                               GroupRepository groupRepository,
                               JobEventRecorder eventRecorder,
                               SchedulingPolicy schedulingPolicy,
                               PostPublisher postPublisher,
                               TimeProvider timeProvider,
                               AutomationPropertiesView automationProperties) {
        this.campaignRepository = campaignRepository;
        this.postJobRepository = postJobRepository;
        this.groupRepository = groupRepository;
        this.eventRecorder = eventRecorder;
        this.schedulingPolicy = schedulingPolicy;
        this.postPublisher = postPublisher;
        this.timeProvider = timeProvider;
        this.automationProperties = automationProperties;
    }

    /** Chọn bài đến hạn, xác nhận nhóm còn cho phép đăng rồi ghi nhận kết quả đăng. */
    @Override
    public DispatchPostResult handle(String campaignId) {
        CampaignId id = CampaignId.of(campaignId);
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new PostingDomainException("CAMPAIGN_NOT_FOUND", "Khong thay campaign " + campaignId));
        if (!campaign.status().canDispatch()) {
            throw new PostingDomainException("CAMPAIGN_NOT_ACTIVE", "Campaign dang " + campaign.status());
        }

        Instant now = timeProvider.now();
        Optional<PostJob> next = postJobRepository.findNextQueued(id, now);
        if (next.isEmpty()) {
            return new DispatchPostResult(false, null, null, "NO_JOB", null, "Khong co bai nao den han", null);
        }

        PostJob job = next.get();
        FbGroup targetGroup = groupRepository.findById(job.groupId())
                .orElseThrow(() -> new PostingDomainException("GROUP_NOT_FOUND",
                        "Không tìm thấy nhóm đã lưu cho bài " + job.id().value()));
        if (!GroupPostingPolicy.isAllowed(targetGroup)) {
            String reason = !targetGroup.postingAllowed()
                    ? "Nhóm " + targetGroup.name() + " đang bị tắt quyền đăng."
                    : GroupPostingPolicy.rejectionReason(targetGroup.name());
            job.cancel(reason, now);
            postJobRepository.save(job);
            eventRecorder.recordStateChanges(job);
            return new DispatchPostResult(false, job.id().value(), job.groupName(),
                    PostStatus.CANCELLED.name(), null, reason, null);
        }
        Instant globalLastPostAt = postJobRepository.findLastPostedAt().orElse(null);
        Instant groupLastPostAt = targetGroup.lastPostAt();

        if (!schedulingPolicy.isAllowed(now, globalLastPostAt, groupLastPostAt)) {
            Instant nextAllowedAt = schedulingPolicy.nextAllowedAt(now, globalLastPostAt, groupLastPostAt);
            return new DispatchPostResult(false, job.id().value(), job.groupName(), "NOT_DUE", null,
                    "Chua den nhip dang, som nhat " + nextAllowedAt, nextAllowedAt);
        }

        job.startComposing(timeProvider.now());
        postJobRepository.save(job);
        eventRecorder.recordStateChanges(job);

        job.startUploading(timeProvider.now());
        postJobRepository.save(job);
        eventRecorder.recordStateChanges(job);

        job.startSubmitting(timeProvider.now());
        postJobRepository.save(job);
        eventRecorder.recordStateChanges(job);

        PublishResult result;
        try {
            result = postPublisher.publish(new PublishRequest(
                    campaign.id().value(),
                    job.id().value(),
                    job.groupId().value(),
                    targetGroup.name(),
                    job.text().content(),
                    campaign.photoPaths(),
                    automationProperties.pageName()
            ));
        } catch (RuntimeException ex) {
            Instant failedAt = timeProvider.now();
            job.fail("Loi khong mong doi khi dang: " + ex.getMessage(), failedAt);
            postJobRepository.save(job);
            eventRecorder.recordStateChanges(job);
            return new DispatchPostResult(true, job.id().value(), job.groupName(), job.status().name(),
                    null, job.errorMessage(), null);
        }

        Instant doneAt = timeProvider.now();
        switch (result.status()) {
            case LIVE -> job.markLive(result.fbPostUrl(), doneAt);
            case PENDING_APPROVAL -> job.markPendingApproval(doneAt);
            case FAILED -> job.fail(result.detail(), doneAt);
            case UNVERIFIED -> eventRecorder.record(job.id(), JobEventType.VERIFY_RESULT,
                    "Đã gửi yêu cầu đăng nhưng chưa tìm thấy bài để xác nhận: " + result.detail(), doneAt);
        }
        postJobRepository.save(job);
        eventRecorder.recordStateChanges(job);
        eventRecorder.record(job.id(), JobEventType.PUBLISH_RESULT, result.detail(), doneAt);

        if (result.isSuccess()) {
            groupRepository.findById(job.groupId())
                    .map(group -> group.withLastPostAt(doneAt))
                    .ifPresent(groupRepository::save);
        }

        return new DispatchPostResult(true, job.id().value(), job.groupName(), job.status().name(),
                job.fbPostUrl(), result.detail(), null);
    }
}
