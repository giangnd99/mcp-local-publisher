package com.gth.posting.ai.adapter;

import com.gth.posting.application.port.out.ai.ContentCritic;
import com.gth.posting.application.port.out.ai.CopyBrief;
import com.gth.posting.application.port.out.ai.CritiqueResult;

/**
 * Fallback khi tat AI: khong cham gi, chi ghi nhan de chuong trinh chay tiep.
 * Cac rang buoc cung (gia, bao hanh, so dien thoai) da duoc ContentPolicy chan o domain.
 */
public class PassThroughContentCritic implements ContentCritic {

    @Override
    public CritiqueResult critique(String text, CopyBrief brief) {
        return CritiqueResult.ok();
    }
}
