package com.gth.posting.domain.valueobject;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * So tien theo dong Viet Nam. amount luon la don vi dong (khong co phan le).
 */
public record Money(long amount, String currency) {

    public static final String VND = "VND";

    public Money {
        if (amount < 0) {
            throw new IllegalArgumentException("Money khong duoc am: " + amount);
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Money can co currency");
        }
    }

    public static Money vnd(long amount) {
        return new Money(amount, VND);
    }

    /** 5200000 -> "5.200.000" */
    public String formatted() {
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.GERMANY);
        format.setGroupingUsed(true);
        return format.format(amount);
    }

    /** Dang dung de doi chieu text bai dang: "5.200.000d" / "5.200.000đ" / "5,200,000" */
    public String digits() {
        return Long.toString(amount);
    }
}
