package com.gth.posting.application.port.out.clock;

import java.time.Instant;

/**
 * Dong ho cua he thong. Tach ra thanh port de test duoc thoi gian
 * va de domain khong bao gio goi Instant.now() truc tiep.
 */
public interface TimeProvider {

    Instant now();
}
