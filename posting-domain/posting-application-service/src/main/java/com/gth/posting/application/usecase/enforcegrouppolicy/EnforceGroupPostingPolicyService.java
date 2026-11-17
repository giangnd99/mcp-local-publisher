package com.gth.posting.application.usecase.enforcegrouppolicy;

import com.gth.posting.application.port.out.clock.TimeProvider;
import com.gth.posting.application.port.out.repository.GroupRepository;
import com.gth.posting.application.port.out.repository.PostJobRepository;
import com.gth.posting.application.support.JobEventRecorder;
import com.gth.posting.domain.entity.FbGroup;
import com.gth.posting.domain.entity.PostJob;
import com.gth.posting.domain.entity.PostStatus;
import com.gth.posting.domain.service.GroupPostingPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/** Đồng bộ quy tắc chủ đề vào danh mục nhóm và các bài chưa được gửi đi. */
@Service
public class EnforceGroupPostingPolicyService implements EnforceGroupPostingPolicyUseCase {

    private static final EnumSet<PostStatus> CANCELLABLE_STATUSES =
            EnumSet.of(PostStatus.DRAFT, PostStatus.VALIDATED, PostStatus.QUEUED, PostStatus.FAILED);

    private final GroupRepository groupRepository;
    private final PostJobRepository postJobRepository;
    private final JobEventRecorder eventRecorder;
    private final TimeProvider timeProvider;

    /** Khởi tạo bộ áp dụng chính sách bằng các cổng lưu trữ và đồng hồ hệ thống. */
    public EnforceGroupPostingPolicyService(GroupRepository groupRepository,
                                            PostJobRepository postJobRepository,
                                            JobEventRecorder eventRecorder,
                                            TimeProvider timeProvider) {
        this.groupRepository = groupRepository;
        this.postJobRepository = postJobRepository;
        this.eventRecorder = eventRecorder;
        this.timeProvider = timeProvider;
    }

    /** Khóa nhóm ngoài chuyên môn rồi hủy bài nháp, đã xếp lịch hoặc có thể thử lại. */
    @Override
    @Transactional
    public GroupPolicyEnforcementResult handle() {
        Instant now = timeProvider.now();
        Map<String, FbGroup> groupsById = new HashMap<>();
        int disabledGroups = 0;
        for (FbGroup group : groupRepository.findAll()) {
            FbGroup current = group;
            if (!GroupPostingPolicy.isTechnologyName(group.name()) && group.postingAllowed()) {
                current = group.withPostingAllowed(false);
                groupRepository.save(current);
                disabledGroups++;
            }
            groupsById.put(current.id().value(), current);
        }

        int cancelledJobs = 0;
        for (PostStatus status : CANCELLABLE_STATUSES) {
            for (PostJob job : postJobRepository.findByStatus(status)) {
                FbGroup group = groupsById.get(job.groupId().value());
                if (GroupPostingPolicy.isAllowed(group)) {
                    continue;
                }
                String reason = group == null
                        ? "Nhóm không còn trong danh sách đã xác nhận."
                        : GroupPostingPolicy.rejectionReason(group.name());
                job.cancel(reason, now);
                postJobRepository.save(job);
                eventRecorder.recordStateChanges(job);
                cancelledJobs++;
            }
        }
        return new GroupPolicyEnforcementResult(disabledGroups, cancelledJobs);
    }
}
