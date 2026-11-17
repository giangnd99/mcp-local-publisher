package com.gth.posting.ai.adapter;

import com.gth.posting.application.port.out.ai.CopyBrief;
import com.gth.posting.application.port.out.ai.Copywriter;
import com.gth.posting.domain.valueobject.ContentObjective;

import java.text.NumberFormat;
import java.util.Locale;

/** Soạn nội dung theo mục tiêu thương hiệu mà không cần gọi dịch vụ AI trả phí. */
public class TemplateCopywriter implements Copywriter {

    /** Tạo bài khác nhau theo mục tiêu chiến dịch và thông tin đã được cung cấp. */
    @Override
    public String write(CopyBrief brief) {
        String region = valueOr(brief.region(), "Bình Dương");
        String group = brief.groupName() == null || brief.groupName().isBlank()
                ? "cộng đồng tại " + region : "nhóm " + brief.groupName();
        return switch (brief.contentObjective()) {
            case BRAND_AWARENESS -> brandAwareness(brief, region, group);
            case EDUCATIONAL -> educational(brief, region, group);
            case SOFT_SELL -> softSell(brief, region, group);
            case DIRECT_SALE -> directSale(brief, region, group);
        };
    }

    /** Viết bài nhận diện bằng lời hứa thương hiệu và lời mời trao đổi không bán gấp. */
    private String brandAwareness(CopyBrief brief, String region, String group) {
        return "Ở " + region + ", khi cần chọn laptop, điều khó nhất thường không phải là chọn máy đắt nhất "
                + "mà là tìm chiếc hợp với việc mình làm và ngân sách mình có.\n\n"
                + brief.brandName() + " bắt đầu từ câu hỏi đó: " + brief.brandPromise() + ".\n"
                + "Máy đang được giới thiệu hôm nay là " + brief.productName()
                + ". Mình chia sẻ để mọi người trong " + group
                + " có thêm một lựa chọn để tham khảo; mỗi người sẽ hợp với một cấu hình khác nhau.\n\n"
                + "Bạn thường dùng máy cho học tập, văn phòng hay sáng tạo? Chia sẻ nhu cầu, GTH cùng bạn tìm hướng chọn phù hợp.";
    }

    /** Chia sẻ cách chọn máy hữu ích rồi gắn vai trò tư vấn của thương hiệu vào chủ đề. */
    private String educational(CopyBrief brief, String region, String group) {
        return "Mẹo chọn laptop cũ ở " + region + ": hãy bắt đầu từ ba điều trước khi nhìn vào tên CPU.\n\n"
                + "1. Việc chính cần làm là gì: học online, Office, đồ họa hay lập trình?\n"
                + "2. Ngân sách có thể chi là bao nhiêu, và có cần nâng cấp sau này không?\n"
                + "3. Hỏi rõ tình trạng máy, pin, phụ kiện và thời hạn bảo hành trước khi quyết định.\n\n"
                + brief.brandName() + " tư vấn theo nhu cầu thực tế để chọn chiếc máy phù hợp, không mặc định cứ cấu hình cao là tốt nhất.\n"
                + "Ví dụ máy đang có thông tin để tham khảo: " + brief.productName() + ".\n\n"
                + "Nếu bạn ở " + group + " và đang phân vân cấu hình nào đủ dùng, hãy để lại nhu cầu và ngân sách; GTH sẽ cùng bạn gỡ từng lựa chọn.";
    }

    /** Giới thiệu sản phẩm như một ví dụ phù hợp nhu cầu và giữ lời kêu gọi ở mức nhẹ. */
    private String softSell(CopyBrief brief, String region, String group) {
        StringBuilder text = new StringBuilder()
                .append("Nếu bạn ở ").append(region)
                .append(" đang tìm máy cho công việc hằng ngày, chiếc phù hợp là chiếc đáp ứng đúng nhu cầu và mức chi của mình.\n\n")
                .append(brief.brandName()).append(" theo cách tư vấn đó: ").append(brief.brandPromise()).append(".\n")
                .append(brief.productName()).append(" là một lựa chọn để anh chị trong ").append(group)
                .append(" tham khảo.");
        appendSpecs(text, brief);
        if (present(brief.warrantyText())) {
            text.append("\nThông tin bảo hành hiện có: ").append(brief.warrantyText()).append('.');
        }
        text.append("\n\nNếu muốn so sánh chiếc này với nhu cầu cụ thể của mình, cứ nhắn GTH để được trao đổi thêm.");
        return text.toString();
    }

    /** Viết bài bán hàng trực tiếp với giá, bảo hành và thông tin liên hệ được cung cấp. */
    private String directSale(CopyBrief brief, String region, String group) {
        StringBuilder text = new StringBuilder()
                .append("Laptop GTH tại ").append(region).append(" giới thiệu ").append(brief.productName())
                .append(" cho anh chị trong ").append(group).append(".\n\n");
        appendSpecs(text, brief);
        if (present(brief.warrantyText())) {
            text.append("Bảo hành: ").append(brief.warrantyText()).append(".\n");
        }
        text.append("Giá: ").append(formatPrice(brief.priceAmount())).append("đ.\n")
                .append("GTH tư vấn theo nhu cầu để anh chị cân nhắc trước khi quyết định.");
        if (present(brief.phoneNumber())) {
            text.append(" Liên hệ/Zalo: ").append(brief.phoneNumber()).append('.');
        }
        return text.toString();
    }

    /** Đưa thông số được cung cấp vào bài và không tự suy đoán cấu hình. */
    private void appendSpecs(StringBuilder text, CopyBrief brief) {
        if (!brief.specs().isEmpty()) {
            text.append("\nThông tin đã xác nhận:");
            brief.specs().forEach(spec -> text.append("\n- ").append(spec));
            text.append('\n');
        }
    }

    /** Định dạng số tiền theo cách hiển thị quen thuộc tại Việt Nam. */
    private String formatPrice(long amount) {
        return NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(amount);
    }

    /** Trả về nội dung thay thế khi trường văn bản bị bỏ trống. */
    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.strip();
    }

    /** Kiểm tra trường tùy chọn có nội dung trước khi đưa vào bài. */
    private boolean present(String value) {
        return value != null && !value.isBlank();
    }
}
