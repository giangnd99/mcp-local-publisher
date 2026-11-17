package com.gth.posting.application.dto;

import com.gth.posting.domain.entity.FbGroup;

import java.time.Instant;

public record GroupDto(
        String groupId,
        String name,
        int memberCount,
        String region,
        boolean postingAllowed,
        Instant lastPostAt
) {

    public static GroupDto from(FbGroup group) {
        return new GroupDto(group.id().value(), group.name(), group.memberCount(), group.region(),
                group.postingAllowed(), group.lastPostAt());
    }
}
