package com.gth.posting.application.usecase.dispatchpost;

import java.time.Instant;

public record DispatchPostResult(
        boolean dispatched,
        String postJobId,
        String groupName,
        String status,
        String fbPostUrl,
        String detail,
        Instant nextAllowedAt
) {
}
