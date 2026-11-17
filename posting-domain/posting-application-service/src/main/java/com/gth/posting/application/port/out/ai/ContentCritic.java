package com.gth.posting.application.port.out.ai;

/**
 * Nguoi phan bien thu hai: mot model khac cham diem bai truoc khi cho dang.
 * Muc dich la chan bia gia, hua sai bao hanh, giong van quang cao qua lo.
 */
public interface ContentCritic {

    CritiqueResult critique(String text, CopyBrief brief);
}
