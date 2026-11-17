package com.gth.posting.domain.valueobject;

/** Mục tiêu nội dung quyết định mức độ giới thiệu sản phẩm và lời mời mua hàng. */
public enum ContentObjective {
    BRAND_AWARENESS,
    EDUCATIONAL,
    SOFT_SELL,
    DIRECT_SALE;

    /** Đọc mục tiêu từ yêu cầu bên ngoài và mặc định về nhận diện thương hiệu. */
    public static ContentObjective from(String value) {
        if (value == null || value.isBlank()) {
            return BRAND_AWARENESS;
        }
        try {
            return valueOf(value.strip().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Mục tiêu nội dung không hợp lệ: " + value);
        }
    }

    /** Cho biết mục tiêu có yêu cầu giá và số liên hệ trong bài hay không. */
    public boolean requiresOfferDetails() {
        return this == DIRECT_SALE;
    }
}
