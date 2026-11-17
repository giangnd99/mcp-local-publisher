package com.gth.posting.application.port.out.publisher;

/**
 * Ket qua that cua mot lan bam Dang.
 * PENDING_APPROVAL khong phai loi: nhieu nhom bat duyet bai truoc khi hien.
 */
public enum PublishStatus {
    LIVE,
    PENDING_APPROVAL,
    /** Facebook nhận thao tác gửi nhưng ứng dụng chưa thấy bài trong feed. */
    UNVERIFIED,
    FAILED
}
