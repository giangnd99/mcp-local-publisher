package com.gth.posting.domain.service;

import com.gth.posting.domain.entity.FbGroup;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Quy tắc chỉ cho phép đăng vào cộng đồng chuyên laptop, PC hoặc công nghệ. */
public final class GroupPostingPolicy {

    private static final List<String> EXCLUDED_PHRASES = List.of(
            "cho", "rao vat", "thanh ly", "sang nhuong", "marketplace"
    );
    private static final Pattern TECHNOLOGY_TOPIC = Pattern.compile(
            "(^| )(laptop|pc|may tinh|cong nghe|linh kien|tin hoc|computer)( |$)"
    );
    private static final Pattern WORD_SEPARATOR = Pattern.compile("[^a-z0-9]+" );

    /** Không cho tạo policy vì đây là bộ quy tắc bất biến, không giữ trạng thái. */
    private GroupPostingPolicy() {
    }

    /** Xác định nhóm vừa thuộc chủ đề công nghệ vừa đang được bật quyền đăng. */
    public static boolean isAllowed(FbGroup group) {
        return group != null && group.postingAllowed() && isTechnologyName(group.name());
    }

    /** Chỉ chấp nhận nhóm có từ khóa ngành hàng và không mang dấu hiệu nhóm chợ/rao vặt. */
    public static boolean isTechnologyName(String groupName) {
        String normalized = normalize(groupName);
        if (normalized.isBlank() || containsExcludedPhrase(normalized)) {
            return false;
        }
        return TECHNOLOGY_TOPIC.matcher(normalized).find();
    }

    /** Trả về lý do tiếng Việt để giao diện và agent giải thích khi từ chối nhóm. */
    public static String rejectionReason(String groupName) {
        if (containsExcludedPhrase(normalize(groupName))) {
            return "Nhóm chợ, rao vặt hoặc thanh lý đã bị loại khỏi phạm vi đăng.";
        }
        return "Chỉ được đăng vào nhóm chuyên laptop, PC, máy tính hoặc công nghệ.";
    }

    /** Chuẩn hóa dấu và dấu câu để bộ lọc hoạt động với tên nhóm tiếng Việt. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String deaccented = Normalizer.normalize(value.toLowerCase(Locale.ROOT).replace('đ', 'd'),
                Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return WORD_SEPARATOR.matcher(deaccented).replaceAll(" ").trim().replaceAll("\\s+", " ");
    }

    /** Kiểm tra nhóm có chứa cụm từ bị loại sau khi đã chuẩn hóa. */
    private static boolean containsExcludedPhrase(String normalizedName) {
        return EXCLUDED_PHRASES.stream().anyMatch(phrase ->
                (" " + normalizedName + " ").contains(" " + phrase + " "));
    }
}
