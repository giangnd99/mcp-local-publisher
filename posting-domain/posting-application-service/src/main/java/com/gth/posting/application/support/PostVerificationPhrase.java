package com.gth.posting.application.support;

import java.util.Arrays;
import java.util.Comparator;

/** Chọn một đoạn nội dung đủ riêng biệt để tìm lại bài trong feed Facebook. */
public final class PostVerificationPhrase {

    private PostVerificationPhrase() {
    }

    /** Ưu tiên dòng dài có nhiều từ riêng và số liệu, rồi giới hạn còn 80 ký tự. */
    public static String select(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String line = text.lines().map(String::strip)
                .filter(value -> value.length() >= 20)
                .max(Comparator.comparingInt(PostVerificationPhrase::specificity))
                .orElseGet(() -> text.strip());
        return line.length() > 80 ? line.substring(0, 80) : line;
    }

    /** Chấm điểm theo độ dài, số từ khác nhau và chữ số để giảm trùng khớp giả. */
    private static int specificity(String line) {
        long distinctWords = Arrays.stream(line.toLowerCase().split("[^\\p{L}\\p{N}]+"))
                .filter(word -> word.length() > 2)
                .distinct()
                .count();
        long digits = line.chars().filter(Character::isDigit).count();
        return (int) Math.min(line.length(), 120) + (int) distinctWords * 4 + (int) digits * 3;
    }
}
