package com.gth.posting.domain.entity;

import com.gth.posting.domain.event.CampaignCreated;
import com.gth.posting.domain.event.DomainEvent;
import com.gth.posting.domain.valueobject.CampaignId;
import com.gth.posting.domain.valueobject.ContentObjective;
import com.gth.posting.domain.valueobject.Money;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Campaign = 1 san pham x N nhom. Giu thong tin "cung" cua san pham
 * (gia, bao hanh, so dien thoai) de moi bai dang dung chung mot nguon su that.
 */
public final class Campaign {

    private final CampaignId id;
    private final String name;
    private final String productName;
    private final Money price;
    private final String warrantyText;
    private final String phoneNumber;
    private final ContentObjective contentObjective;
    private final String brandName;
    private final boolean brandNameRequired;
    private final List<String> photoPaths;

    private CampaignStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<DomainEvent> pendingEvents = new ArrayList<>();

    private Campaign(CampaignId id,
                     String name,
                     String productName,
                     Money price,
                     String warrantyText,
                     String phoneNumber,
                     ContentObjective contentObjective,
                     String brandName,
                     boolean brandNameRequired,
                     List<String> photoPaths,
                     CampaignStatus status,
                     Instant createdAt,
                     Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.productName = productName;
        this.price = price;
        this.warrantyText = warrantyText;
        this.phoneNumber = phoneNumber;
        this.contentObjective = contentObjective;
        this.brandName = brandName;
        this.brandNameRequired = brandNameRequired;
        this.photoPaths = List.copyOf(photoPaths);
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Tạo chiến dịch mới với mục tiêu, thương hiệu và sự kiện tạo tương ứng. */
    public static Campaign create(String name,
                                 String productName,
                                 Money price,
                                 String warrantyText,
                                 String phoneNumber,
                                 ContentObjective contentObjective,
                                 String brandName,
                                 List<String> photoPaths,
                                 Instant now) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Campaign can co ten");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Campaign can co ten san pham");
        }
        Campaign campaign = new Campaign(CampaignId.newId(), name.strip(), productName.strip(), price,
                warrantyText == null ? "" : warrantyText.strip(), phoneNumber == null ? "" : phoneNumber.strip(),
                contentObjective == null ? ContentObjective.BRAND_AWARENESS : contentObjective,
                normalizeBrandName(brandName),
                true,
                photoPaths == null ? List.of() : photoPaths, CampaignStatus.DRAFT, now, now);
        campaign.pendingEvents.add(new CampaignCreated(campaign.id, campaign.name, 0, now));
        return campaign;
    }

    /** Khôi phục chiến dịch cùng quy tắc thương hiệu từ dữ liệu lưu trữ. */
    public static Campaign restore(CampaignId id,
                                   String name,
                                   String productName,
                                   Money price,
                                   String warrantyText,
                                   String phoneNumber,
                                   ContentObjective contentObjective,
                                   String brandName,
                                   boolean brandNameRequired,
                                   List<String> photoPaths,
                                   CampaignStatus status,
                                   Instant createdAt,
                                   Instant updatedAt) {
        return new Campaign(id, name, productName, price, warrantyText, phoneNumber,
                contentObjective == null ? ContentObjective.DIRECT_SALE : contentObjective,
                normalizeBrandName(brandName),
                brandNameRequired,
                photoPaths, status, createdAt, updatedAt);
    }

    public void activate(Instant now) {
        this.status = CampaignStatus.ACTIVE;
        this.updatedAt = now;
    }

    public void pause(Instant now) {
        this.status = CampaignStatus.PAUSED;
        this.updatedAt = now;
    }

    public void complete(Instant now) {
        this.status = CampaignStatus.COMPLETED;
        this.updatedAt = now;
    }

    public List<DomainEvent> pullPendingEvents() {
        List<DomainEvent> copy = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return copy;
    }

    public CampaignId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String productName() {
        return productName;
    }

    public Money price() {
        return price;
    }

    public String warrantyText() {
        return warrantyText;
    }

    public String phoneNumber() {
        return phoneNumber;
    }

    /** Trả về mục tiêu nội dung đã lưu cùng chiến dịch. */
    public ContentObjective contentObjective() {
        return contentObjective;
    }

    /** Trả về thương hiệu cần xuất hiện trong bài đăng của chiến dịch. */
    public String brandName() {
        return brandName;
    }

    /** Cho biết chiến dịch mới có bắt buộc nhắc tên thương hiệu trong bài hay không. */
    public boolean brandNameRequired() {
        return brandNameRequired;
    }

    /** Chuẩn hóa tên thương hiệu và dùng Laptop GTH khi yêu cầu cũ không cung cấp tên. */
    private static String normalizeBrandName(String brandName) {
        return brandName == null || brandName.isBlank() ? "Laptop GTH" : brandName.strip();
    }

    public List<String> photoPaths() {
        return photoPaths;
    }

    public CampaignStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
