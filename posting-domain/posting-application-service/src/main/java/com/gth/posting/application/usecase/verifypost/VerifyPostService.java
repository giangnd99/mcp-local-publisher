package com.gth.posting.application.usecase.verifypost;

import com.gth.posting.application.port.out.clock.TimeProvider;
import com.gth.posting.application.port.out.publisher.PostVerifier;
import com.gth.posting.application.port.out.publisher.VerifyRequest;
import com.gth.posting.application.port.out.publisher.VerifyResult;
import com.gth.posting.application.port.out.repository.PostJobRepository;
import com.gth.posting.application.support.JobEventRecorder;
import com.gth.posting.application.support.PostVerificationPhrase;
import com.gth.posting.domain.entity.JobEventType;
import com.gth.posting.domain.entity.PostJob;
import com.gth.posting.domain.entity.PostStatus;
import com.gth.posting.domain.exception.PostingDomainException;
import com.gth.posting.domain.valueobject.PostJobId;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class VerifyPostService implements VerifyPostUseCase {

    private final PostJobRepository postJobRepository;
    private final PostVerifier postVerifier;
    private final JobEventRecorder eventRecorder;
    private final TimeProvider timeProvider;

    public VerifyPostService(PostJobRepository postJobRepository,
                             PostVerifier postVerifier,
                             JobEventRecorder eventRecorder,
                             TimeProvider timeProvider) {
        this.postJobRepository = postJobRepository;
        this.postVerifier = postVerifier;
        this.eventRecorder = eventRecorder;
        this.timeProvider = timeProvider;
    }

    /** Đối chiếu bài trong feed đúng nhóm và chỉ chuyển sang LIVE khi tìm được nội dung. */
    @Override
    public VerifyPostResult handle(String postJobId) {
        PostJob job = postJobRepository.findById(PostJobId.of(postJobId))
                .orElseThrow(() -> new PostingDomainException("JOB_NOT_FOUND", "Khong thay job " + postJobId));

        if (job.status() != PostStatus.PENDING_APPROVAL && job.status() != PostStatus.SUBMITTING) {
            throw new PostingDomainException("JOB_NOT_VERIFIABLE",
                    "Job dang " + job.status() + ", chi verify duoc bai dang cho duyet");
        }

        String phrase = PostVerificationPhrase.select(job.text().content());
        VerifyResult result = postVerifier.verify(new VerifyRequest(
                job.groupId().value(), job.groupName(), phrase, job.id().value()));
        Instant now = timeProvider.now();

        switch (result.status()) {
            case LIVE -> {
                String postUrl = result.fbPostUrl() == null ? job.fbPostUrl() : result.fbPostUrl();
                job.markLive(postUrl, now);
                postJobRepository.save(job);
                eventRecorder.recordStateChanges(job);
            }
            case PENDING_APPROVAL -> eventRecorder.record(job.id(), JobEventType.VERIFY_RESULT,
                    "Van dang cho duyet: " + result.detail(), now);
            case NOT_FOUND -> eventRecorder.record(job.id(), JobEventType.VERIFY_RESULT,
                    "Khong tim thay bai trong feed: " + result.detail(), now);
        }

        return new VerifyPostResult(job.id().value(), job.groupName(), job.status().name(),
                result.status() + " - " + result.detail());
    }

}
