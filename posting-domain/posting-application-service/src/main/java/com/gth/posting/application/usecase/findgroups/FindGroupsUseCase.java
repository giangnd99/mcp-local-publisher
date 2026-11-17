package com.gth.posting.application.usecase.findgroups;

import java.util.List;

/** Tra cứu các nhóm đã lưu theo mã, tên hoặc khu vực. */
public interface FindGroupsUseCase {

    /** Trả về tối đa limit nhóm phù hợp nhất với từ khóa. */
    List<GroupSearchResult> handle(String query, int limit);
}
