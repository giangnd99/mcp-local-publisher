package com.gth.posting.application.usecase.findgroups;

import com.gth.posting.application.dto.GroupSummaryDto;

/** Kết quả tìm nhóm đã lưu, kèm điểm khớp để agent chọn đúng mục tiêu. */
public record GroupSearchResult(GroupSummaryDto group, int score, String matchReason) {
}
