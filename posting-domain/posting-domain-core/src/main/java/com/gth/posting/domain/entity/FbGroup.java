package com.gth.posting.domain.entity;

import com.gth.posting.domain.valueobject.GroupId;

import java.time.Instant;

/**
 * Nhom Facebook dich. Bat bien (immutable): khi nhom doi trang thai,
 * tao ban ghi moi bang {@link #withLastPostAt(Instant)} / {@link #withPostingAllowed(boolean)}.
 */
public record FbGroup(
        GroupId id,
        String name,
        int memberCount,
        String region,
        boolean postingAllowed,
        Instant lastPostAt
) {

    public FbGroup {
        if (id == null) {
            throw new IllegalArgumentException("FbGroup can GroupId");
        }
        name = name == null ? "" : name.strip();
        region = region == null ? "" : region.strip();
        if (memberCount < 0) {
            throw new IllegalArgumentException("memberCount khong duoc am");
        }
    }

    /** Nhom du dieu kien dua vao campaign: du thanh vien va con cho dang. */
    public boolean isEligible(int minMemberCount) {
        return postingAllowed && memberCount >= minMemberCount;
    }

    public FbGroup withLastPostAt(Instant postedAt) {
        return new FbGroup(id, name, memberCount, region, postingAllowed, postedAt);
    }

    public FbGroup withPostingAllowed(boolean allowed) {
        return new FbGroup(id, name, memberCount, region, allowed, lastPostAt);
    }
}
