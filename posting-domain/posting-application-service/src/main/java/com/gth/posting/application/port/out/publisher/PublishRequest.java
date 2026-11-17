package com.gth.posting.application.port.out.publisher;

import java.util.List;

/**
 * Yeu cau dang bai. Day la du lieu thuan, adapter MCP/Playwright tu quyet dinh
 * cach thuc hien tung buoc tren trinh duyet.
 */
public record PublishRequest(
        String campaignId,
        String postJobId,
        String groupId,
        String groupName,
        String text,
        List<String> photoPaths,
        String pageName
) {

    public PublishRequest {
        photoPaths = photoPaths == null ? List.of() : List.copyOf(photoPaths);
    }
}
