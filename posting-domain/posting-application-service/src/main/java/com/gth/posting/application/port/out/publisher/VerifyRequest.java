package com.gth.posting.application.port.out.publisher;

public record VerifyRequest(String groupId, String groupName, String uniquePhrase, String postJobId) {
}
