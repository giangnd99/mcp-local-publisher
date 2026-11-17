package com.gth.posting.automation.adapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm tra các điều kiện an toàn quan trọng trong JavaScript điều khiển Facebook. */
class FacebookScriptsTest {

    /** Kịch bản xác minh phải yêu cầu đúng mã nhóm và tên tiêu đề trước khi soạn bài. */
    @Test
    void verifiesGroupIdAndName() {
        String script = FacebookScripts.verifyGroup("123456789", "Chợ Laptop Bình Dương");

        assertTrue(script.contains("GROUP_ID_MISMATCH"));
        assertTrue(script.contains("GROUP_NAME_MISMATCH"));
        assertTrue(script.contains("GROUP_VERIFIED"));
    }

    /** Cho phép đăng bài chữ ngắn và không ảnh, đồng thời vẫn kiểm tra số dòng. */
    @Test
    void allowsTextOnlyPostsWithoutFixedLengthThreshold() {
        String script = FacebookScripts.submit(1, 0);

        assertTrue(script.contains("blobs < 0"));
        assertFalse(script.contains("len > 400"));
    }

    /** Xác minh bài phải tìm nội dung trong thẻ bài thay vì chỉ khớp ở toàn trang. */
    @Test
    void verifiesPhraseInsideFeedArticle() {
        String script = FacebookScripts.verifyPublishedPost("ThinkPad T480 i5 8350U", 3, 500);

        assertTrue(script.contains("querySelectorAll('[role=\"article\"]')"));
        assertTrue(script.contains("POST_VERIFIED"));
        assertTrue(script.contains("POST_UNVERIFIED"));
    }
}
