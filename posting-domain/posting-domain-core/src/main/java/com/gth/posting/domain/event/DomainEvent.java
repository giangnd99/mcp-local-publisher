package com.gth.posting.domain.event;

import java.time.Instant;

/**
 * Su kien nghiep vu. Chi la du lieu, khong biet ai lang nghe.
 */
public interface DomainEvent {

    Instant occurredAt();
}
