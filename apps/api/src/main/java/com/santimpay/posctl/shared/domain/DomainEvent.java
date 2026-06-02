package com.santimpay.posctl.shared.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Marker for cross-module integration events. Implementations are immutable records living in a
 * module's {@code events} package — they ARE the module's published contract. They are captured into
 * the transactional outbox in the same DB transaction as the state change, then relayed to Redis
 * Streams and to in-process listeners (see {@code shared.outbox}).
 */
public interface DomainEvent {

    /** Id of the aggregate that produced the event. */
    UUID aggregateId();

    /** Logical aggregate type, e.g. {@code "merchant"}. */
    String aggregateType();

    /** Event name; defaults to the simple class name, e.g. {@code "MerchantActivated"}. */
    default String eventType() {
        return getClass().getSimpleName();
    }

    /** When the event occurred. */
    default Instant occurredAt() {
        return Instant.now();
    }
}
