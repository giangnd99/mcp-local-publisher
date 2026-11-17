package com.gth.posting.automation.adapter;

import com.gth.posting.application.port.out.publisher.PostVerifier;
import com.gth.posting.application.port.out.publisher.VerifyRequest;
import com.gth.posting.application.port.out.publisher.VerifyResult;
import com.gth.posting.automation.config.AutomationProperties;
import com.gth.posting.automation.mcpgateway.McpToolGateway;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kiem chung bai da len feed chua.
 * Day la buoc quyet dinh: "da bam Dang" va "bai da hien" la hai chuyen khac nhau.
 */
@Component
public class ChromeMcpPostVerifier implements PostVerifier {

    private static final Pattern TAB_ID = Pattern.compile("(?:\"tabId\"\\s*:\\s*|\\btab(?:Id)?\\s*[:=]\\s*|\\bID:\\s*)(\\d+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern POST_URL = Pattern.compile("FEED_FOUND URL=(https?://\\S+)");

    private final McpToolGateway gateway;
    private final AutomationProperties properties;

    public ChromeMcpPostVerifier(McpToolGateway gateway, AutomationProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
    }

    /** Mở đúng feed nhóm, xác minh tên nhóm rồi tìm câu riêng của bài đăng. */
    @Override
    public VerifyResult verify(VerifyRequest request) {
        if (!gateway.isAvailable()) {
            return VerifyResult.notFound("Chua ket noi MCP server automation");
        }

        Map<String, Object> args = new LinkedHashMap<>();
        args.put("url", properties.getGroupsBaseUrl() + request.groupId() + "?sorting_setting=CHRONOLOGICAL");
        if (properties.getWindowId() > 0) {
            args.put("windowId", properties.getWindowId());
        }
        if (properties.getTabId() > 0) {
            args.put("tabId", properties.getTabId());
        } else {
            args.put("newWindow", true);
        }
        Optional<String> navigation = gateway.call(properties.getTools().getNavigate(), args);
        if (navigation.isEmpty() || !isSuccessfulToolResult(navigation.get())) {
            return VerifyResult.notFound("Khong mo duoc tab nhom " + request.groupId());
        }

        Matcher tabMatcher = TAB_ID.matcher(navigation.get());
        long tabId = tabMatcher.find() ? Long.parseLong(tabMatcher.group(1)) : properties.getTabId();
        if (tabId <= 0) {
            return VerifyResult.notFound("MCP không trả mã tab Facebook để xác minh nhóm.");
        }
        sleep(properties.getNavigateWaitMs());

        Map<String, Object> groupCheckArgs = new LinkedHashMap<>();
        groupCheckArgs.put("tabId", tabId);
        groupCheckArgs.put("code", FacebookScripts.verifyGroup(request.groupId(), request.groupName()));
        Optional<String> groupCheck = gateway.call(properties.getTools().getJavascript(), groupCheckArgs)
                .filter(ChromeMcpPostVerifier::isSuccessfulToolResult);
        if (groupCheck.isEmpty() || !groupCheck.get().contains("GROUP_VERIFIED")) {
            return VerifyResult.notFound("Không xác minh được feed đúng nhóm: "
                    + groupCheck.orElse("không có phản hồi từ Facebook"));
        }

        Map<String, Object> jsArgs = new LinkedHashMap<>();
        jsArgs.put("tabId", tabId);
        jsArgs.put("code", FacebookScripts.scrollAndFindPhrase(request.uniquePhrase(),
                properties.getVerifyScrollTimes(), properties.getVerifyScrollStepMs()));

        Optional<String> scanned = gateway.call(properties.getTools().getJavascript(), jsArgs)
                .filter(ChromeMcpPostVerifier::isSuccessfulToolResult);
        if (scanned.isEmpty()) {
            return VerifyResult.notFound("Khong doc duoc feed nhom " + request.groupId());
        }

        String payload = scanned.get();
        if (payload.contains(FacebookScripts.FEED_FOUND)) {
            Matcher urlMatcher = POST_URL.matcher(payload);
            String postUrl = urlMatcher.find() ? urlMatcher.group(1) : null;
            return VerifyResult.live(postUrl, payload);
        }
        if (payload.contains("PENDING")) {
            return VerifyResult.pendingApproval(payload);
        }
        return VerifyResult.notFound("Khong thay bai trong feed: " + payload);
    }

    /** Loại phản hồi MCP lỗi trước khi xem chúng là bằng chứng đã xác minh. */
    /** Loại phản hồi MCP lỗi trước khi xem chúng là bằng chứng đã xác minh. */
    private static boolean isSuccessfulToolResult(String result) {
        return result != null && !result.contains("[isError=true]")
                && !result.startsWith("Error calling tool:");
    }

    /** Chờ giao diện tải feed và giữ lại cờ ngắt luồng nếu có. */
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
