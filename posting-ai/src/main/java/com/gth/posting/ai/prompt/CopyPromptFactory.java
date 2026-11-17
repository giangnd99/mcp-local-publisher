package com.gth.posting.ai.prompt;

import com.gth.posting.application.port.out.ai.CopyBrief;
import com.gth.posting.domain.valueobject.ContentObjective;

/** Tập trung hướng dẫn soạn và kiểm duyệt để mọi adapter dùng cùng chiến lược thương hiệu. */
public final class CopyPromptFactory {

    private CopyPromptFactory() {
    }

    /** Tạo chỉ dẫn hệ thống theo mục tiêu, bảo đảm bài không mặc định thành quảng cáo bán hàng. */
    public static String system(String tone, CopyBrief brief) {
        String goal = switch (brief.contentObjective()) {
            case BRAND_AWARENESS -> "Mục tiêu: giúp người đọc nhớ Laptop GTH và cách tư vấn phù hợp nhu cầu. Không đưa giá, số điện thoại hoặc thúc ép mua.";
            case EDUCATIONAL -> "Mục tiêu: chia sẻ kiến thức hữu ích về chọn, dùng hoặc chăm sóc laptop; gắn tự nhiên với cách GTH tư vấn. Không biến bài thành bảng rao bán.";
            case SOFT_SELL -> "Mục tiêu: nêu tình huống sử dụng và lợi ích phù hợp, giới thiệu sản phẩm như lựa chọn tham khảo; lời mời trao đổi nhẹ nhàng.";
            case DIRECT_SALE -> "Mục tiêu: giới thiệu sản phẩm để người đọc cân nhắc mua. Giá, bảo hành và số liên hệ phải khớp nguyên văn dữ liệu.";
        };
        return """
                Bạn là người viết nội dung cho thương hiệu %s.
                Viết bằng tiếng Việt tự nhiên, tôn trọng người đọc trong nhóm Facebook và nói rõ giá trị của thương hiệu.

                Mục tiêu chiến dịch: %s
                Định vị đã xác nhận: %s

                Nguyên tắc bắt buộc:
                - Bám đúng mục tiêu chiến dịch; không tự thêm giá hay số điện thoại nếu mục tiêu không cần.
                - Nêu tên thương hiệu tự nhiên ít nhất một lần và làm rõ GTH giúp khách chọn máy theo nhu cầu, ngân sách.
                - Chỉ dùng thông số, tình trạng, chính sách và sự kiện có trong dữ liệu; không tự nhận máy tốt, pin tốt hay còn hàng.
                - Không dựng đánh giá, lời chứng thực, số lượng tồn kho, ưu đãi hoặc cam kết dịch vụ.
                - Không dùng câu giật gân, khan hiếm giả, lặp từ khóa hoặc hashtag dày đặc.
                - Viết gọn, chia đoạn dễ đọc trên điện thoại; thay đổi góc mở bài theo nhóm.
                - Giọng văn: %s.

                Chỉ trả về nội dung bài đăng, không giải thích.
                """.formatted(brief.brandName(), goal, brief.brandPromise(),
                tone == null ? "thân thiện, rõ ràng và trung thực" : tone);
    }

    /** Tạo dữ liệu đầu vào có cấu trúc để mô hình chỉ sử dụng sự thật đã được cung cấp. */
    public static String user(CopyBrief brief) {
        StringBuilder sb = new StringBuilder();
        sb.append("Mục tiêu: ").append(brief.contentObjective()).append('\n');
        sb.append("Tên thương hiệu: ").append(brief.brandName()).append('\n');
        sb.append("Định vị: ").append(brief.brandPromise()).append('\n');
        sb.append("Sản phẩm tham khảo: ").append(brief.productName()).append('\n');
        if (brief.contentObjective() == ContentObjective.DIRECT_SALE
                || brief.contentObjective() == ContentObjective.SOFT_SELL) {
            sb.append("Giá: ").append(formatPrice(brief.priceAmount())).append("đ\n");
            appendIfPresent(sb, "Bảo hành", brief.warrantyText());
            appendIfPresent(sb, "Liên hệ/Zalo", brief.phoneNumber());
        }
        if (!brief.specs().isEmpty()) {
            sb.append("Thông tin kỹ thuật đã xác nhận:\n");
            brief.specs().forEach(spec -> sb.append("- ").append(spec).append('\n'));
        }
        appendIfPresent(sb, "Nhóm đăng", brief.groupName());
        appendIfPresent(sb, "Khu vực", brief.region());
        sb.append("\nHãy tạo một bài riêng, có ích với người đọc trong nhóm này.");
        return sb.toString();
    }

    /** Tạo tiêu chí rà soát phù hợp mục tiêu và giảm nguy cơ nội dung quảng cáo sai sự thật. */
    public static String criticSystem() {
        return """
                Bạn kiểm duyệt nội dung cho thương hiệu bán và chăm sóc máy tính.
                Chấm theo các tiêu chí:
                1. Nội dung có nhắc đúng tên thương hiệu và đúng định vị cung cấp không?
                2. Có bịa cấu hình, tình trạng, giá, bảo hành, tồn kho, ưu đãi hay đánh giá không?
                3. Có đúng mục tiêu chiến dịch, hữu ích và tự nhiên với cộng đồng không?
                4. Chỉ mục tiêu DIRECT_SALE mới bắt buộc giá; bảo hành và liên hệ phải khớp nếu được cung cấp.
                5. Có câu thúc ép, giật gân, placeholder hoặc lặp nội dung vô nghĩa không?

                Trả về duy nhất JSON: {"acceptable": true|false, "issues": ["..."]}
                """;
    }

    /** Cung cấp dữ liệu đối chiếu và nội dung cần chấm cho bộ kiểm duyệt AI. */
    public static String criticUser(String text, CopyBrief brief) {
        return """
                Mục tiêu: %s
                Thương hiệu: %s
                Định vị: %s
                Sản phẩm tham khảo: %s
                Giá đã cung cấp: %sd
                Bảo hành đã cung cấp: %s
                Số liên hệ đã cung cấp: %s

                Nội dung cần chấm:
                ---
                %s
                ---
                """.formatted(brief.contentObjective(), brief.brandName(), brief.brandPromise(),
                brief.productName(), formatPrice(brief.priceAmount()), brief.warrantyText(), brief.phoneNumber(), text);
    }

    /** Thêm một trường dữ liệu khi giá trị được cung cấp, tránh in các giá trị null. */
    private static void appendIfPresent(StringBuilder output, String label, String value) {
        if (value != null && !value.isBlank()) {
            output.append(label).append(": ").append(value.strip()).append('\n');
        }
    }

    /** Định dạng giá tiền thành nhóm ba chữ số để đối chiếu đúng với bài viết. */
    private static String formatPrice(long amount) {
        return java.text.NumberFormat.getIntegerInstance(java.util.Locale.forLanguageTag("vi-VN")).format(amount);
    }
}
