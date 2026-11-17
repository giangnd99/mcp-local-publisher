package com.gth.posting.domain.service;

import com.gth.posting.domain.valueobject.ContentObjective;
import com.gth.posting.domain.valueobject.Money;
import com.gth.posting.domain.valueobject.PostText;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Bảo đảm mục tiêu nhận diện thương hiệu không bị áp điều kiện của bài chốt đơn. */
class ContentPolicyObjectiveTest {

    /** Chấp nhận bài nhận diện đủ dài có tên thương hiệu dù không có giá hay số điện thoại. */
    @Test
    void acceptsBrandAwarenessWithoutOfferDetails() {
        ContentPolicy policy = new ContentPolicy();
        ContentPolicy.Requirement requirement = new ContentPolicy.Requirement(
                Money.vnd(7_500_000), "6 tháng", "0900000000",
                ContentObjective.BRAND_AWARENESS, "Laptop GTH");
        PostText text = PostText.of("Laptop GTH giúp người dùng bắt đầu từ nhu cầu và ngân sách để chọn máy phù hợp. "
                + "Khi cân nhắc laptop cũ, hãy hỏi rõ tình trạng, pin, phụ kiện và chính sách bảo hành. "
                + "Bạn dùng máy cho học tập, văn phòng hay sáng tạo? Chia sẻ mục đích để cùng trao đổi lựa chọn.");

        assertTrue(policy.validate(text, requirement).valid());
    }

    /** Bài bán hàng vẫn phải thể hiện giá và liên hệ đã cam kết trong dữ liệu chiến dịch. */
    @Test
    void requiresOfferDetailsForDirectSale() {
        ContentPolicy policy = new ContentPolicy();
        ContentPolicy.Requirement requirement = new ContentPolicy.Requirement(
                Money.vnd(7_500_000), "6 tháng", "0900000000",
                ContentObjective.DIRECT_SALE, "Laptop GTH");
        PostText text = PostText.of("Laptop GTH giới thiệu ThinkPad T480 cho công việc văn phòng. "
                + "Máy có cấu hình phù hợp nhu cầu cơ bản và khách hàng có thể hỏi thêm để biết chi tiết.");

        assertFalse(policy.validate(text, requirement).valid());
    }
}
