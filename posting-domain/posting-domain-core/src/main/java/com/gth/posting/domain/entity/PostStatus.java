package com.gth.posting.domain.entity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Vong doi cua mot bai dang. Day la state machine, khong phai co boolean.
 *
 * DRAFT -> VALIDATED -> QUEUED -> COMPOSING -> UPLOADING -> SUBMITTING
 *                                                        |-> LIVE
 *                                                        |-> PENDING_APPROVAL -> LIVE | REJECTED
 *                                                        |-> REJECTED
 *                                                        |-> FAILED -> QUEUED (retry) | CANCELLED
 *       DRAFT | VALIDATED | QUEUED --------------------------------------> CANCELLED
 */
public enum PostStatus {

    DRAFT,
    VALIDATED,
    QUEUED,
    COMPOSING,
    UPLOADING,
    SUBMITTING,
    LIVE,
    PENDING_APPROVAL,
    REJECTED,
    FAILED,
    CANCELLED;

    private static final Map<PostStatus, Set<PostStatus>> ALLOWED = Map.ofEntries(
            Map.entry(DRAFT, EnumSet.of(VALIDATED, FAILED, CANCELLED)),
            Map.entry(VALIDATED, EnumSet.of(QUEUED, FAILED, CANCELLED)),
            Map.entry(QUEUED, EnumSet.of(COMPOSING, FAILED, CANCELLED)),
            Map.entry(COMPOSING, EnumSet.of(UPLOADING, FAILED)),
            Map.entry(UPLOADING, EnumSet.of(SUBMITTING, FAILED)),
            Map.entry(SUBMITTING, EnumSet.of(LIVE, PENDING_APPROVAL, REJECTED, FAILED)),
            Map.entry(PENDING_APPROVAL, EnumSet.of(LIVE, REJECTED, FAILED)),
            Map.entry(REJECTED, EnumSet.noneOf(PostStatus.class)),
            Map.entry(FAILED, EnumSet.of(QUEUED, CANCELLED)),
            Map.entry(LIVE, EnumSet.noneOf(PostStatus.class)),
            Map.entry(CANCELLED, EnumSet.noneOf(PostStatus.class))
    );

    public boolean canTransitionTo(PostStatus target) {
        if (target == null) {
            return false;
        }
        return ALLOWED.getOrDefault(this, Collections.emptySet()).contains(target);
    }

    public Set<PostStatus> allowedTargets() {
        return Collections.unmodifiableSet(ALLOWED.getOrDefault(this, Collections.emptySet()));
    }

    /** Trang thai ket thuc: khong con tac dong tu dong nao nua. */
    public boolean isTerminal() {
        return this == LIVE || this == REJECTED || this == CANCELLED;
    }

    /** Da gui len Facebook nhung chua biet ket qua cuoi. */
    public boolean isInFlight() {
        return this == COMPOSING || this == UPLOADING || this == SUBMITTING;
    }
}
