package com.gth.posting.domain.valueobject;

/**
 * Noi dung bai dang. Giu nguyen van ban nguoi dung/AI sinh ra,
 * chi chuan hoa khoang trang thua o dau va cuoi.
 */
public record PostText(String content) {

    public static final int MIN_LENGTH = 80;
    public static final int MAX_LENGTH = 8000;

    public PostText {
        if (content == null) {
            throw new IllegalArgumentException("PostText khong duoc null");
        }
        content = content.strip();
        if (content.isEmpty()) {
            throw new IllegalArgumentException("PostText khong duoc rong");
        }
        if (content.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("PostText vuot qua " + MAX_LENGTH + " ky tu");
        }
    }

    public static PostText of(String content) {
        return new PostText(content);
    }

    public int length() {
        return content.length();
    }

    /** So sanh khong phan biet hoa thuong, da bo dau tieng Viet de bat trung gan dung. */
    public String normalized() {
        String lower = content.toLowerCase(java.util.Locale.ROOT);
        String noAccent = java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd');
        return noAccent.replaceAll("\\s+", " ").trim();
    }

    @Override
    public String toString() {
        return content;
    }
}
