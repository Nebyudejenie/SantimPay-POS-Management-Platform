package com.santimpay.posctl.shared.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.santimpay.posctl.shared.domain.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Captures {@link DomainEvent}s raised by aggregates into the outbox <em>in the same transaction</em>
 * as the state change (Spring publishes aggregate events during {@code save}). This is the heart of
 * the at-least-once delivery guarantee — no distributed transaction needed (ADR-004 / ADR-015).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventOutboxListener {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void onDomainEvent(DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            repository.save(OutboxEvent.of(
                    event.aggregateType(),
                    event.aggregateId(),
                    event.eventType(),
                    payload,
                    event.occurredAt()));
            log.debug("Outboxed {} for {}:{}", event.eventType(),
                    event.aggregateType(), event.aggregateId());
        } catch (JsonProcessingException e) {
            // Fail the transaction: we must never lose an event silently.
            throw new IllegalStateException("Failed to serialize domain event " + event.eventType(), e);
        }
    }
}
