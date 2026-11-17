package com.gth.posting.dao.campaign.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "campaign")
public class CampaignEntity {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "price_amount", nullable = false)
    private long priceAmount;

    @Column(name = "warranty_text")
    private String warrantyText;

    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    @Column(name = "content_objective", nullable = false, length = 32)
    private String contentObjective;

    @Column(name = "brand_name", nullable = false, length = 255)
    private String brandName;

    @Column(name = "brand_name_required", nullable = false)
    private boolean brandNameRequired;

    /** Danh sach duong dan anh, ngan cach bang dau | */
    @Column(name = "photo_paths", columnDefinition = "text")
    private String photoPaths;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CampaignEntity() {
    }

    /** Tạo entity lưu trữ từ các trường đã chuyển đổi ở adapter data-access. */
    public CampaignEntity(String id, String name, String productName, long priceAmount, String warrantyText,
                          String phoneNumber, String contentObjective, String brandName, boolean brandNameRequired,
                          String photoPaths, String status,
                          Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.productName = productName;
        this.priceAmount = priceAmount;
        this.warrantyText = warrantyText;
        this.phoneNumber = phoneNumber;
        this.contentObjective = contentObjective;
        this.brandName = brandName;
        this.brandNameRequired = brandNameRequired;
        this.photoPaths = photoPaths;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProductName() {
        return productName;
    }

    public long getPriceAmount() {
        return priceAmount;
    }

    public String getWarrantyText() {
        return warrantyText;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    /** Trả về tên mục tiêu nội dung dùng để kiểm tra chiến dịch khi đăng. */
    public String getContentObjective() {
        return contentObjective;
    }

    /** Trả về tên thương hiệu được lưu để kiểm tra các bài trong chiến dịch. */
    public String getBrandName() {
        return brandName;
    }

    /** Trả về cờ kiểm tra tên thương hiệu đã lưu cùng chiến dịch. */
    public boolean isBrandNameRequired() {
        return brandNameRequired;
    }

    public String getPhotoPaths() {
        return photoPaths;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
