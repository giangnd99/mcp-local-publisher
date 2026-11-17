package com.gth.posting.application.port.out.publisher;

/**
 * @param fbPostUrl co the null khi bai cho duyet hoac that bai
 * @param detail    thong tin chan doan: so anh da upload, do dai text, thong bao cua FB
 */
public record PublishResult(PublishStatus status, String fbPostUrl, String detail) {

    public static PublishResult live(String fbPostUrl, String detail) {
        return new PublishResult(PublishStatus.LIVE, fbPostUrl, detail);
    }

    public static PublishResult pendingApproval(String detail) {
        return new PublishResult(PublishStatus.PENDING_APPROVAL, null, detail);
    }

    /** Ghi nhận Facebook đã nhận lệnh nhưng cần tìm lại bài trước khi xác nhận LIVE. */
    public static PublishResult unverified(String detail) {
        return new PublishResult(PublishStatus.UNVERIFIED, null, detail);
    }

    public static PublishResult failed(String detail) {
        return new PublishResult(PublishStatus.FAILED, null, detail);
    }

    public boolean isSuccess() {
        return status == PublishStatus.LIVE || status == PublishStatus.PENDING_APPROVAL;
    }
}
