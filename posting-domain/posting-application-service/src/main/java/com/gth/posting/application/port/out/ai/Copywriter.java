package com.gth.posting.application.port.out.ai;

public interface Copywriter {

    /** Sinh mot bai dang rieng cho mot nhom cu the. */
    String write(CopyBrief brief);
}
