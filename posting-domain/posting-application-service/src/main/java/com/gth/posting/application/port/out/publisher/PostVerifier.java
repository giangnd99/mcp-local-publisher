package com.gth.posting.application.port.out.publisher;

/**
 * Kiem chung bai da len that chua. "Da bam Dang" khong dong nghia "da len feed":
 * khoang 40-50% bai vao hang cho duyet cua quan tri vien nhom.
 */
public interface PostVerifier {

    VerifyResult verify(VerifyRequest request);
}
