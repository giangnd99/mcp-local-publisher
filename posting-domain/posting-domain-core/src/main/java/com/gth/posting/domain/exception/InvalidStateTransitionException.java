package com.gth.posting.domain.exception;

import com.gth.posting.domain.entity.PostStatus;

/**
 * Chuyen trang thai khong hop le. Vi du: bai dang da LIVE ma doi sang QUEUED.
 */
public class InvalidStateTransitionException extends PostingDomainException {

    public static final String CODE = "INVALID_STATE_TRANSITION";

    public InvalidStateTransitionException(PostStatus from, PostStatus to) {
        super(CODE, "Khong the chuyen tu " + from + " sang " + to
                + ". Chi cho phep: " + from.allowedTargets());
    }
}
