package com.gth.posting.application.dto;

/**
 * Mang cấu hình mà người vận hành có thể chỉnh trên màn hình thiết lập.
 * Bí mật chỉ được dùng để ghi cấu hình và không được đưa thẳng ra giao diện.
 */
public record SetupSettings(
        String databaseUrl,
        String databaseUser,
        String databasePassword,
        boolean databasePasswordConfigured,
        boolean mcpEnabled,
        String chromeCommand,
        String chromeScript,
        boolean aiEnabled,
        String openAiApiKey,
        boolean openAiApiKeyConfigured,
        String pageName,
        long globalGapMinutes,
        long groupGapHours) {
}
