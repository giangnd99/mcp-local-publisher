package com.gth.posting.domain.valueobject;

import java.util.UUID;

/**
 * Identifier cua mot post job (mot bai dang vao mot nhom cu the).
 */
public record PostJobId(String value) {

    public PostJobId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PostJobId khong duoc rong");
        }
    }

    public static PostJobId of(String value) {
        return new PostJobId(value);
    }

    public static PostJobId newId() {
        return new PostJobId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
