package com.gth.posting.application.usecase.createcampaign;

import com.gth.posting.domain.entity.FbGroup;

/** Nhóm đã được xác nhận cùng nội dung cuối cùng trước khi ghi chiến dịch. */
record PreparedGroupContent(FbGroup group, String text) {
}
