package com.gth.posting.domain.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm tra bộ lọc chủ đề loại nhóm chợ nhưng vẫn giữ cộng đồng công nghệ chuyên biệt. */
class GroupPostingPolicyTest {

    /** Cho phép nhóm chuyên laptop, PC, linh kiện và công nghệ dù tên có dấu tiếng Việt. */
    @Test
    void allowsSpecializedTechnologyGroups() {
        assertTrue(GroupPostingPolicy.isTechnologyName("Laptop và PC Bình Dương"));
        assertTrue(GroupPostingPolicy.isTechnologyName("Linh Kiện Máy Tính Cũ"));
        assertTrue(GroupPostingPolicy.isTechnologyName("Mua bán laptop cũ Bình Dương"));
        assertTrue(GroupPostingPolicy.isTechnologyName("Cộng đồng Công nghệ thông tin"));
    }

    /** Chặn chợ, rao vặt và nhóm cộng đồng không chuyên máy tính. */
    @Test
    void rejectsMarketsAndUnrelatedGroups() {
        assertFalse(GroupPostingPolicy.isTechnologyName("CHỢ PC - LAPTOP BÌNH DƯƠNG"));
        assertFalse(GroupPostingPolicy.isTechnologyName("Chợ Online Thủ Dầu Một"));
        assertFalse(GroupPostingPolicy.isTechnologyName("RAO VẶT BÌNH DƯƠNG"));
        assertFalse(GroupPostingPolicy.isTechnologyName("Cộng đồng cư dân Thuận An"));
    }
}
