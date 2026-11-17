package com.gth.posting.application.usecase.enforcegrouppolicy;

/** Cổng thực thi quy tắc chỉ đăng vào nhóm laptop, PC và công nghệ. */
public interface EnforceGroupPostingPolicyUseCase {

    /** Khóa nhóm không phù hợp và hủy các bài chưa được gửi đến những nhóm đó. */
    GroupPolicyEnforcementResult handle();
}
