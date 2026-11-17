package com.gth.posting.automation.adapter;

import com.gth.posting.application.support.PostVerificationPhrase;
import com.gth.posting.application.port.out.publisher.PostPublisher;
import com.gth.posting.application.port.out.publisher.PublishRequest;
import com.gth.posting.application.port.out.publisher.PublishResult;
import com.gth.posting.automation.config.AutomationProperties;
import com.gth.posting.automation.mcpgateway.McpToolGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dang bai qua MCP server chrome (chrome-devtools-mcp / mcp-chrome-bridge)
 * tren Chrome dang dang nhap that cua may shop.
 *
 * Khong copy cookie, khong luu mat khau: toan bo phien nam trong Chrome cua nguoi dung.
 */
@Component
public class ChromeMcpPostPublisher implements PostPublisher {

    private static final Logger log = LoggerFactory.getLogger(ChromeMcpPostPublisher.class);
    private static final Pattern TAB_ID = Pattern.compile("(?:\"tabId\"\\s*:\\s*|\\btab(?:Id)?\\s*[:=]\\s*|\\bID:\\s*)(\\d+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern POST_URL = Pattern.compile("POST_VERIFIED url=(https?://\\S+)");

    private final McpToolGateway gateway;
    private final AutomationProperties properties;

    public ChromeMcpPostPublisher(McpToolGateway gateway, AutomationProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
    }

    /** Mở đúng nhóm, kiểm tra nội dung/ảnh, gửi bài rồi xác minh bài xuất hiện trong feed. */
    @Override
    public PublishResult publish(PublishRequest request) {
        if (!gateway.isAvailable()) {
            return PublishResult.failed("Chua ket noi MCP server automation. Bat spring.ai.mcp.client.enabled=true "
                    + "va tro toi server chrome.");
        }

        long tabId = openGroupTab(request.groupId());
        if (tabId <= 0) {
            return PublishResult.failed("Khong mo duoc tab nhom " + request.groupId());
        }

        sleep(properties.getNavigateWaitMs());

        Optional<String> verifiedGroup = callJs(tabId,
                FacebookScripts.verifyGroup(request.groupId(), request.groupName()));
        if (verifiedGroup.isEmpty() || !verifiedGroup.get().contains("GROUP_VERIFIED")) {
            return PublishResult.failed("Từ chối đăng vì chưa xác minh được đúng nhóm. "
                    + verifiedGroup.orElse("Không nhận được phản hồi từ Facebook."));
        }

        Optional<String> composer = callJs(tabId, FacebookScripts.clickComposer());
        if (composer.isEmpty() || !composer.get().contains(FacebookScripts.COMPOSER_OK)) {
            return PublishResult.failed("Nhom khong co o soan bai (co the chi quan tri vien duoc dang): "
                    + composer.orElse("khong co phan hoi"));
        }

        sleep(properties.getComposerWaitMs());

        Optional<String> inserted = callJs(tabId,
                FacebookScripts.insertText(request.text(), request.photoPaths().size()));
        if (inserted.isEmpty() || !inserted.get().contains(FacebookScripts.INSERT_OK)) {
            return PublishResult.failed("Khong chen duoc noi dung: " + inserted.orElse("khong co phan hoi"));
        }

        for (String photoPath : request.photoPaths()) {
            Optional<String> uploaded = upload(tabId, photoPath);
            if (uploaded.isEmpty() || !isSuccessfulToolResult(uploaded.get())) {
                log.warn("Tải ảnh lên Facebook thất bại: {}", photoPath);
                return PublishResult.failed("Không tải đủ ảnh lên Facebook; đã dừng trước khi bấm Đăng.");
            }
        }

        sleep(properties.getUploadWaitMs());

        Optional<String> submitted = Optional.empty();
        for (int attempt = 0; attempt < 4; attempt++) {
            submitted = callJs(tabId, FacebookScripts.submit(
                    FacebookScripts.countLines(request.text()), request.photoPaths().size()));
            if (submitted.isPresent() && submitted.get().contains(FacebookScripts.SUBMIT_OK)) {
                break;
            }
            if (submitted.isEmpty() || !submitted.get().contains("SUBMIT_WAIT")) {
                return PublishResult.failed("Không thể gửi bài: "
                        + submitted.orElse("Không nhận được phản hồi từ Facebook."));
            }
            sleep(1000L);
        }
        if (submitted.isEmpty() || !submitted.get().contains(FacebookScripts.SUBMIT_OK)) {
            return PublishResult.failed("Facebook chưa bật nút Đăng hoặc chưa nhận đủ ảnh/nội dung: "
                    + submitted.orElse("không có phản hồi"));
        }

        sleep(properties.getPostWaitMs());

        String detail = "tabId=" + tabId + " | " + submitted.orElse("");

        Optional<String> verification = callJs(tabId, FacebookScripts.verifyPublishedPost(
                PostVerificationPhrase.select(request.text()), 4, properties.getVerifyScrollStepMs()));
        if (verification.isPresent() && verification.get().contains("POST_PENDING_APPROVAL")) {
            return PublishResult.pendingApproval(detail + " | " + verification.get());
        }
        if (verification.isPresent() && verification.get().contains("POST_VERIFIED")) {
            Matcher urlMatcher = POST_URL.matcher(verification.get());
            String postUrl = urlMatcher.find() ? urlMatcher.group(1) : null;
            return PublishResult.live(postUrl, detail + " | " + verification.get());
        }
        return PublishResult.unverified(detail + " | "
                + verification.orElse("Chưa đọc được feed để xác minh bài đã xuất hiện."));
    }

    /** Mở URL của mã nhóm đã đăng ký và trả về mã tab do Chrome cấp. */
    private long openGroupTab(String groupId) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("url", properties.getGroupsBaseUrl() + groupId);

        if (properties.getTabId() > 0) {
            args.put("tabId", properties.getTabId());
            if (properties.getWindowId() > 0) {
                args.put("windowId", properties.getWindowId());
            }
        } else {
            args.put("newWindow", true);
        }

        Optional<String> result = gateway.call(properties.getTools().getNavigate(), args);
        if (result.isEmpty() || !isSuccessfulToolResult(result.get())) {
            log.warn("chrome_navigate khong tra ve ket qua cho nhom {}", groupId);
            return -1;
        }
        Matcher matcher = TAB_ID.matcher(result.get());
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        if (properties.getTabId() > 0) {
            return properties.getTabId();
        }
        log.warn("Không đọc được tabId từ phản hồi chrome_navigate: {}", result.get());
        return -1;
    }

    /** Chạy JavaScript trên tab đã chọn và loại kết quả báo lỗi từ MCP. */
    private Optional<String> callJs(long tabId, String code) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("tabId", tabId);
        args.put("code", code);
        Optional<String> result = gateway.call(properties.getTools().getJavascript(), args);
        return result.filter(ChromeMcpPostPublisher::isSuccessfulToolResult);
    }

    /** Gửi một đường dẫn ảnh cục bộ đến input ảnh trong trình soạn bài. */
    private Optional<String> upload(long tabId, String photoPath) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("tabId", tabId);
        args.put("selector", "#fb_up");
        args.put("filePath", photoPath);
        args.put("multiple", true);
        return gateway.call(properties.getTools().getUploadFile(), args);
    }

    /** Loại kết quả báo lỗi do MCP gateway gói trong phần văn bản trả về. */
    /** Kiểm tra gateway không đánh dấu kết quả công cụ là lỗi. */
    private static boolean isSuccessfulToolResult(String result) {
        return result != null && !result.contains("[isError=true]")
                && !result.startsWith("Error calling tool:");
    }

    /** Chờ giao diện hoàn tất bước hiện tại và giữ lại cờ ngắt luồng nếu có. */
    private void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
