package com.gth.posting.api.dto;

import java.util.List;

public record CreateCampaignRequest(
        String name,
        String productName,
        Long priceAmount,
        String warrantyText,
        String phoneNumber,
        String contentObjective,
        List<String> productFacts,
        String brandName,
        String brandPromise,
        List<String> photoPaths,
        List<GroupItem> groups
) {

    /** Duy trì tương thích với JSON cũ và chọn nhận diện thương hiệu làm mục tiêu mặc định. */
    public CreateCampaignRequest(String name, String productName, long priceAmount, String warrantyText,
                                  String phoneNumber, List<String> photoPaths, List<GroupItem> groups) {
        this(name, productName, priceAmount, warrantyText, phoneNumber,
                "BRAND_AWARENESS", List.of(), "Laptop GTH", null, photoPaths, groups);
    }

    /** Duy trì tương thích với yêu cầu đã truyền mục tiêu nhưng chưa có thông tin chi tiết sản phẩm. */
    public CreateCampaignRequest(String name, String productName, long priceAmount, String warrantyText,
                                 String phoneNumber, String contentObjective,
                                 List<String> photoPaths, List<GroupItem> groups) {
        this(name, productName, priceAmount, warrantyText, phoneNumber,
                contentObjective, List.of(), "Laptop GTH", null, photoPaths, groups);
    }

    public record GroupItem(String groupId, String name, int memberCount, String region, String text) {
    }
}
