package com.gth.posting.domain.valueobject;

/**
 * Facebook group id (day so, vi du 1135263994445887).
 * Khac voi ten nhom: ten nhom co the doi, gid thi khong.
 */
public record GroupId(String value) {

    public GroupId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("GroupId khong duoc rong");
        }
        String trimmed = value.trim();
        if (!trimmed.matches("\\d{5,25}")) {
            throw new IllegalArgumentException("GroupId phai la chuoi so (5-25 ky tu): " + value);
        }
        value = trimmed;
    }

    public static GroupId of(String value) {
        return new GroupId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
