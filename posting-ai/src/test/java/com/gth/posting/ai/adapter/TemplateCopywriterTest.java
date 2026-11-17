package com.gth.posting.ai.adapter;

import com.gth.posting.application.port.out.ai.CopyBrief;
import com.gth.posting.domain.valueobject.ContentObjective;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Bảo đảm chế độ không dùng AI vẫn tạo nội dung thật và riêng cho nhóm. */
class TemplateCopywriterTest {

    /** Mẫu phải dùng dữ liệu đầu vào, đạt độ dài tối thiểu và không bịa tình trạng máy. */
    @Test
    void writesUsefulGroupSpecificCopyWithoutInventedCondition() {
        CopyBrief brief = new CopyBrief("ThinkPad T480", 7_500_000, "bảo hành 6 tháng",
                "0900000000", List.of(), "Chợ Laptop Thủ Dầu Một", "Bình Dương", "thân thiện",
                ContentObjective.DIRECT_SALE, "Laptop GTH", "hiểu nhu cầu và chọn máy phù hợp");

        String content = new TemplateCopywriter().write(brief);

        assertTrue(content.length() >= 120);
        assertTrue(content.contains("ThinkPad T480"));
        assertTrue(content.contains("Chợ Laptop Thủ Dầu Một"));
        assertTrue(content.contains("7.500.000"));
        assertTrue(content.contains("bảo hành 6 tháng"));
        assertTrue(content.contains("0900000000"));
        assertFalse(content.contains("Máy dùng ổn"));
    }

    /** Bài nhận diện cần quảng bá định vị mà không buộc đưa giá và liên hệ vào nội dung. */
    @Test
    void writesBrandAwarenessCopyWithoutSalesCallToAction() {
        CopyBrief brief = new CopyBrief("ThinkPad T480", 7_500_000, "bảo hành 6 tháng",
                "0900000000", List.of(), "Chợ Laptop Thủ Dầu Một", "Bình Dương", "thân thiện",
                ContentObjective.BRAND_AWARENESS, "Laptop GTH", "hiểu nhu cầu, chọn máy phù hợp");

        String content = new TemplateCopywriter().write(brief);

        assertTrue(content.contains("Laptop GTH"));
        assertTrue(content.contains("hiểu nhu cầu, chọn máy phù hợp"));
        assertTrue(content.contains("ThinkPad T480"));
        assertFalse(content.contains("7.500.000"));
        assertFalse(content.contains("0900000000"));
    }
}
