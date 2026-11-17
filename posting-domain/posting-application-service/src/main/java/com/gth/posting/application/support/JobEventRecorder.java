package com.gth.posting.application.support;

import com.gth.posting.application.port.out.repository.JobEventRepository;
import com.gth.posting.domain.entity.JobEvent;
import com.gth.posting.domain.entity.JobEventType;
import com.gth.posting.domain.entity.PostJob;
import com.gth.posting.domain.event.DomainEvent;
import com.gth.posting.domain.event.PostJobStateChanged;
import com.gth.posting.domain.valueobject.PostJobId;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Ghi lai moi thay doi trang thai thanh job_event.
 * Day la "hop den" cua audit trail: khong co no thi khong tra loi duoc
 * vi sao mot bai khong len feed.
 */
@Component
public class JobEventRecorder {

    private final JobEventRepository jobEventRepository;

    public JobEventRecorder(JobEventRepository jobEventRepository) {
        this.jobEventRepository = jobEventRepository;
    }

    public void recordStateChanges(PostJob job) {
        List<DomainEvent> events = job.pullPendingEvents();
        for (DomainEvent event : events) {
            if (event instanceof PostJobStateChanged changed) {
                jobEventRepository.save(JobEvent.of(changed.postJobId(), JobEventType.STATE_CHANGED,
                        changed.from() + " -> " + changed.to()
                                + (changed.reason() == null ? "" : " (" + changed.reason() + ")"),
                        changed.occurredAt()));
            }
        }
    }

    public void record(PostJobId jobId, JobEventType type, String payload, Instant now) {
        jobEventRepository.save(JobEvent.of(jobId, type, payload, now));
    }
}
