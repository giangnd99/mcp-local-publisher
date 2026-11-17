package com.gth.posting.application.port.out.publisher;

public record VerifyResult(VerifyStatus status, String fbPostUrl, String detail) {

    public static VerifyResult live(String detail) {
        return new VerifyResult(VerifyStatus.LIVE, null, detail);
    }

    /** Tạo kết quả xác minh thành công kèm liên kết bài Facebook nếu tìm được. */
    public static VerifyResult live(String fbPostUrl, String detail) {
        return new VerifyResult(VerifyStatus.LIVE, fbPostUrl, detail);
    }

    public static VerifyResult pendingApproval(String detail) {
        return new VerifyResult(VerifyStatus.PENDING_APPROVAL, null, detail);
    }

    public static VerifyResult notFound(String detail) {
        return new VerifyResult(VerifyStatus.NOT_FOUND, null, detail);
    }
}
