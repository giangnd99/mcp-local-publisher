package com.gth.posting.application.port.out.ai;

import com.gth.posting.domain.valueobject.ContentObjective;

import java.util.List;

/**
 * Thong tin dau vao de AI viet bai. Cang cu the cang it bia dat.
 */
public record CopyBrief(
        String productName,
        long priceAmount,
        String warrantyText,
        String phoneNumber,
        List<String> specs,
        String groupName,
        String region,
        String tone,
        ContentObjective contentObjective,
        String brandName,
        String brandPromise
) {

    /** Chuẩn hóa dữ liệu tùy chọn để adapter nội dung luôn nhận được danh sách và thương hiệu hợp lệ. */
    public CopyBrief {
        specs = specs == null ? List.of() : List.copyOf(specs);
        contentObjective = contentObjective == null ? ContentObjective.BRAND_AWARENESS : contentObjective;
        brandName = brandName == null || brandName.isBlank() ? "Laptop GTH" : brandName.strip();
        brandPromise = brandPromise == null || brandPromise.isBlank()
                ? "hiểu nhu cầu, chọn máy phù hợp và chăm sóc thiết bị trong quá trình sử dụng"
                : brandPromise.strip();
    }

    /** Giữ cách khởi tạo cũ để các bộ chuyển đổi hiện tại có thể nâng cấp dần. */
    public CopyBrief(String productName, long priceAmount, String warrantyText, String phoneNumber,
                     List<String> specs, String groupName, String region, String tone) {
        this(productName, priceAmount, warrantyText, phoneNumber, specs, groupName, region, tone,
                ContentObjective.BRAND_AWARENESS, "Laptop GTH",
                "hiểu nhu cầu, chọn máy phù hợp và chăm sóc thiết bị trong quá trình sử dụng");
    }
}
