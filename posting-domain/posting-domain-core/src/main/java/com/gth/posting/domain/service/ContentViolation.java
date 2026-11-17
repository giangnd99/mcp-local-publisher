package com.gth.posting.domain.service;

/**
 * Mot loi cu the tim thay trong noi dung bai dang.
 */
public record ContentViolation(String code, String message) {

    public static ContentViolation of(String code, String message) {
        return new ContentViolation(code, message);
    }
}
