package com.santimpay.posctl.shared.outbox;

import com.santimpay.posctl.shared.domain.UuidV7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Transactional outbox row (maps to {@code notification.outbox}). Written in the same transaction as
 * the aggregate change; a relay later publishes it and flips {@code dispatched}.
 */
@Getter
@Entity
@Table(name = "outbox", schema = "notification")
public class OutboxEvent {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "aggregate_type", nullable = false, updatable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, updatable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, updatable = false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, updatable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "dispatched", nullable = false)
    private boolean dispatched;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    protected OutboxEvent() {}

    public static OutboxEvent of(String aggregateType, UUID aggregateId, String eventType,
                                 String payloadJson, Instant occurredAt) {
        OutboxEvent e = new OutboxEvent();
        e.id = UuidV7.generate();
        e.aggregateType = aggregateType;
        e.aggregateId = aggregateId;
        e.eventType = eventType;
        e.payload = payloadJson;
        e.occurredAt = occurredAt;
        e.dispatched = false;
        e.attempts = 0;
        return e;
    }

    void markDispatched() {
        this.dispatched = true;
        this.dispatchedAt = Instant.now();
    }

    void recordAttempt() {
        this.attempts++;
    }
}
