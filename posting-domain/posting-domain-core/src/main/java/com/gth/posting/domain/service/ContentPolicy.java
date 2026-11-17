package com.gth.posting.domain.service;

import com.gth.posting.domain.valueobject.Money;
import com.gth.posting.domain.valueobject.PostText;
import com.gth.posting.domain.valueobject.ContentObjective;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Chinh sach noi dung: nhung gi mot bai dang bat buoc phai co va bi cam.
 *
 * Day la nghiep vu thuan tuy nen nam o domain-core: khong doc DB, khong goi API.
 * Tang application chi viec goi va dung ket qua.
 */
public final class ContentPolicy {

    public static final int MIN_LENGTH = 120;
    public static final int MAX_LENGTH = 6000;

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{?[a-zA-Z0-9_]+}?}|<[a-zA-Z0-9_]+>");
    private static final List<String> FORBIDDEN_TERMS = List.of(
            "dxdiag", "lorem ipsum", "todo", "xxx", "noi dung mau"
    );

    /** Thong tin bat buoc cua campaign, dung de doi chieu voi text. */
    public record Requirement(Money price, String warrantyText, String phoneNumber,
                             ContentObjective objective, String brandName, boolean brandNameRequired) {

        /** Giữ tương thích cho nơi kiểm tra cũ bằng cách xem bài là bán hàng trực tiếp. */
        public Requirement(Money price, String warrantyText, String phoneNumber) {
            this(price, warrantyText, phoneNumber, ContentObjective.DIRECT_SALE, "", false);
        }

        /** Giữ tương thích cho bài cũ; chỉ bắt buộc thương hiệu ở mục tiêu không bán trực tiếp. */
        public Requirement(Money price, String warrantyText, String phoneNumber,
                          ContentObjective objective, String brandName) {
            this(price, warrantyText, phoneNumber, objective, brandName,
                    objective != ContentObjective.DIRECT_SALE);
        }

        /** Chuẩn hóa mục tiêu và tên thương hiệu trước khi áp dụng các điều kiện nghiệp vụ. */
        public Requirement {
            objective = objective == null ? ContentObjective.BRAND_AWARENESS : objective;
            brandName = brandName == null ? "" : brandName.strip();
        }
    }

    /** Kiểm tra độ dài, dữ kiện bắt buộc theo mục tiêu và sự hiện diện của thương hiệu. */
    public ContentValidationResult validate(PostText text, Requirement requirement) {
        if (text == null) {
            return ContentValidationResult.of(List.of(ContentViolation.of("TEXT_MISSING", "Bai dang khong co noi dung")));
        }
        List<ContentViolation> violations = new ArrayList<>();
        String content = text.content();
        String plain = text.normalized();
        String digits = onlyDigits(content);

        if (content.length() < MIN_LENGTH) {
            violations.add(ContentViolation.of("TOO_SHORT",
                    "Noi dung " + content.length() + " ky tu, toi thieu " + MIN_LENGTH));
        }
        if (content.length() > MAX_LENGTH) {
            violations.add(ContentViolation.of("TOO_LONG",
                    "Noi dung " + content.length() + " ky tu, toi da " + MAX_LENGTH));
        }
        if (PLACEHOLDER.matcher(content).find()) {
            violations.add(ContentViolation.of("PLACEHOLDER", "Con placeholder chua duoc dien"));
        }
        for (String term : FORBIDDEN_TERMS) {
            if (plain.contains(term)) {
                violations.add(ContentViolation.of("FORBIDDEN_TERM", "Chua cum tu bi cam: " + term));
            }
        }
        if (requirement != null) {
            if (requirement.objective().requiresOfferDetails() && requirement.price() != null
                    && !digits.contains(requirement.price().digits())) {
                violations.add(ContentViolation.of("PRICE_MISSING",
                        "Thieu gia " + requirement.price().formatted()));
            }
            if (requirement.objective().requiresOfferDetails() && isPresent(requirement.phoneNumber())) {
                String phoneDigits = normalizePhone(requirement.phoneNumber());
                if (!normalizePhone(digits).contains(phoneDigits)) {
                    violations.add(ContentViolation.of("PHONE_MISSING",
                            "Thieu so dien thoai " + requirement.phoneNumber()));
                }
            }
            if (requirement.objective().requiresOfferDetails() && isPresent(requirement.warrantyText())
                    && !plain.contains(normalizePlain(requirement.warrantyText()))) {
                violations.add(ContentViolation.of("WARRANTY_MISSING",
                        "Thieu thong tin bao hanh: " + requirement.warrantyText()));
            }
            if (requirement.brandNameRequired() && isPresent(requirement.brandName())
                    && !plain.contains(normalizePlain(requirement.brandName()))) {
                violations.add(ContentViolation.of("BRAND_MISSING",
                        "Bài đăng cần nhắc đến thương hiệu " + requirement.brandName()));
            }
        }
        return ContentValidationResult.of(violations);
    }

    /**
     * Hai bai trong cung campaign khong duoc trung nhau (ke ca chi doi dau cau).
     * Tra ve danh sach vi pham theo vi tri trong danh sach dau vao.
     */
    public List<ContentViolation> validateUniqueness(List<PostText> texts) {
        List<ContentViolation> violations = new ArrayList<>();
        if (texts == null) {
            return violations;
        }
        for (int i = 0; i < texts.size(); i++) {
            if (texts.get(i) == null) {
                continue;
            }
            String current = texts.get(i).normalized();
            for (int j = 0; j < i; j++) {
                if (texts.get(j) == null) {
                    continue;
                }
                if (current.equals(texts.get(j).normalized())) {
                    violations.add(ContentViolation.of("DUPLICATE_TEXT",
                            "Bai thu " + i + " trung bai thu " + j));
                    break;
                }
            }
        }
        return violations;
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    /** Bo dau + ha chu thuong + gop khoang trang, dung cho ca hai phia khi so khop. */
    private static String normalizePlain(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        String noAccent = java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd');
        return noAccent.replaceAll("\\s+", " ").trim();
    }

    private static String onlyDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    /** 0779755739 / +84779755739 / 0779 755 739 -> 779755739 (bo 0 va 84 o dau) */
    private static String normalizePhone(String value) {
        String digits = onlyDigits(value);
        if (digits.startsWith("84")) {
            digits = digits.substring(2);
        }
        if (digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        return digits;
    }
}
