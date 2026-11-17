package com.gth.posting.domain.service;

import java.time.Duration;
import java.time.Instant;

/**
 * Chinh sach nhip dang: giu khoang cach giua cac bai de giong nguoi that.
 *
 * Co 2 rang buoc:
 *  - global: toan bo tai khoan khong dang 2 bai sat nhau
 *  - per-group: cung mot nhom phai cho lau hon nua
 */
public final class SchedulingPolicy {

    private final Duration globalGap;
    private final Duration groupGap;

    public SchedulingPolicy(Duration globalGap, Duration groupGap) {
        if (globalGap == null || globalGap.isNegative()) {
            throw new IllegalArgumentException("globalGap khong hop le");
        }
        if (groupGap == null || groupGap.isNegative()) {
            throw new IllegalArgumentException("groupGap khong hop le");
        }
        this.globalGap = globalGap;
        this.groupGap = groupGap;
    }

    public Instant nextAllowedAt(Instant now, Instant lastGlobalPostAt, Instant groupLastPostAt) {
        Instant result = now;
        if (lastGlobalPostAt != null) {
            Instant candidate = lastGlobalPostAt.plus(globalGap);
            if (candidate.isAfter(result)) {
                result = candidate;
            }
        }
        if (groupLastPostAt != null) {
            Instant candidate = groupLastPostAt.plus(groupGap);
            if (candidate.isAfter(result)) {
                result = candidate;
            }
        }
        return result;
    }

    public boolean isAllowed(Instant now, Instant lastGlobalPostAt, Instant groupLastPostAt) {
        return !nextAllowedAt(now, lastGlobalPostAt, groupLastPostAt).isAfter(now);
    }

    public Duration globalGap() {
        return globalGap;
    }

    public Duration groupGap() {
        return groupGap;
    }
}
