package com.gth.posting.application.usecase.enforcegrouppolicy;

/** Kết quả cập nhật danh sách nhóm và các bài chưa gửi thuộc nhóm bị loại. */
public record GroupPolicyEnforcementResult(int disabledGroups, int cancelledJobs) {
}
