package com.gth.posting.application.dto;

import java.time.Instant;

/** Thông tin nhóm Facebook đủ dùng cho màn hình và agent lựa chọn đích đăng. */
public record GroupSummaryDto(
        String groupId,
        String name,
        int memberCount,
        String region,
        boolean postingAllowed,
        Instant lastPostAt) {
}
