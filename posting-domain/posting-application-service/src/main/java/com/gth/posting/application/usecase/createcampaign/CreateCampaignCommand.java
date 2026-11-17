package com.gth.posting.application.usecase.createcampaign;

import com.gth.posting.domain.valueobject.ContentObjective;

import java.util.List;

/**
 * Lenh tao campaign. Moi nhom kem theo text rieng: he thong khong tu nhan ban
 * mot bai cho nhieu nhom, vi do la ly do pho bien nhat khien FB gan co spam.
 */
public record CreateCampaignCommand(
        String name,
        String productName,
        long priceAmount,
        String warrantyText,
        String phoneNumber,
        ContentObjective contentObjective,
        List<String> productFacts,
        String brandName,
        String brandPromise,
        List<String> photoPaths,
        List<GroupInput> groups
) {

    /** Giữ tương thích với tác vụ cũ và mặc định nội dung sang nhận diện thương hiệu. */
    public CreateCampaignCommand(String name, String productName, long priceAmount, String warrantyText,
                                 String phoneNumber, List<String> photoPaths, List<GroupInput> groups) {
        this(name, productName, priceAmount, warrantyText, phoneNumber,
                ContentObjective.BRAND_AWARENESS, List.of(), "Laptop GTH", null, photoPaths, groups);
    }

    /** Giữ tương thích cho lời gọi đã có mục tiêu nhưng chưa truyền dữ kiện sản phẩm. */
    public CreateCampaignCommand(String name, String productName, long priceAmount, String warrantyText,
                                 String phoneNumber, ContentObjective contentObjective,
                                 List<String> photoPaths, List<GroupInput> groups) {
        this(name, productName, priceAmount, warrantyText, phoneNumber,
                contentObjective, List.of(), "Laptop GTH", null, photoPaths, groups);
    }

    /** Chuẩn hóa các danh sách và giá trị thương hiệu tùy chọn trước khi vào use case. */
    public CreateCampaignCommand {
        photoPaths = photoPaths == null ? List.of() : List.copyOf(photoPaths);
        groups = groups == null ? List.of() : List.copyOf(groups);
        contentObjective = contentObjective == null ? ContentObjective.BRAND_AWARENESS : contentObjective;
        productFacts = productFacts == null ? List.of() : List.copyOf(productFacts);
        brandName = brandName == null || brandName.isBlank() ? "Laptop GTH" : brandName.strip();
        brandPromise = brandPromise == null ? "" : brandPromise.strip();
    }

    public record GroupInput(
            String groupId,
            String name,
            int memberCount,
            String region,
            String text
    ) {
    }
}
