package com.gth.posting.application.usecase.listgroups;

import com.gth.posting.application.dto.GroupSummaryDto;

import java.util.List;

/** Ca sử dụng truy vấn danh sách nhóm Facebook đã biết. */
public interface ListGroupsUseCase {

    /** Trả về các nhóm để người dùng hoặc agent chọn nơi đăng bài. */
    List<GroupSummaryDto> handle();
}
