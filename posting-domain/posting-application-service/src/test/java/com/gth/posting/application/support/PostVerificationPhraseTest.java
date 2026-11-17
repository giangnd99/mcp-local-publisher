package com.gth.posting.application.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm tra đoạn văn bản được chọn làm dấu hiệu tìm lại bài đã đăng. */
class PostVerificationPhraseTest {

    /** Chọn dòng có nội dung riêng thay vì câu mở đầu chung chung. */
    @Test
    void prefersSpecificLine() {
        String content = "Mình chia sẻ thông tin sản phẩm mới.\n" +
                "ThinkPad T480 i5 8350U RAM 16GB SSD 512GB giá 7.500.000 đồng, bảo hành 6 tháng.";

        assertTrue(PostVerificationPhrase.select(content).startsWith("ThinkPad T480"));
    }

    /** Cắt câu xác minh ở 80 ký tự để giảm nguy cơ lệch do giao diện định dạng. */
    @Test
    void limitsPhraseLength() {
        assertEquals(80, PostVerificationPhrase.select("A".repeat(120)).length());
    }

    /** Văn bản rỗng trả về dấu hiệu rỗng, không vô tình khớp mọi bài trong feed. */
    @Test
    void returnsEmptyForBlankContent() {
        assertEquals("", PostVerificationPhrase.select("  "));
    }
}
