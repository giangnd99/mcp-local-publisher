package com.gth.posting.domain.exception;

/**
 * Loi nghiep vu goc cua tang domain. Cac tang ngoai bat exception nay
 * va dich sang HTTP/message phu hop, domain khong biet HTTP la gi.
 */
public class PostingDomainException extends RuntimeException {

    private final String code;

    public PostingDomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public PostingDomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
