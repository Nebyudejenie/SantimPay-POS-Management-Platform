package com.santimpay.posctl.shared.outbox;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Polls the outbox and publishes undispatched events to a Redis Stream
 * ({@code posctl.events.<aggregateType>}), then marks them dispatched. At-least-once; consumers must
 * be idempotent (dedupe on the event id). A small batch + short interval keeps lag low; the
 * {@code ix_outbox_undispatched} partial index keeps the poll cheap.
 *
 * <p>Replace with logical-decoding CDC (Debezium) if poll pressure ever becomes material.
 */
/**
 * Only instantiated when {@code posctl.worker.enabled=true} (the worker Deployment). The API pods
 * run with it disabled so the relay's scheduled poll runs in exactly one place — no N-way duplicate
 * polling across API replicas. (Delivery is idempotent regardless, but single-runner is cheaper and
 * keeps Redis-stream ordering sane.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "posctl.worker.enabled", havingValue = "true")
public class OutboxRelay {

    private static final int BATCH = 200;
    private final OutboxEventRepository repository;
    private final StringRedisTemplate redis;

    @Scheduled(fixedDelayString = "${posctl.outbox.relay-interval-ms:1000}")
    @Transactional
    public void relay() {
        List<OutboxEvent> batch = repository.findUndispatched(Limit.of(BATCH));
        if (batch.isEmpty()) return;

        for (OutboxEvent event : batch) {
            try {
                String stream = "posctl.events." + event.aggregateType();
                redis.opsForStream().add(StreamRecords.mapBacked(Map.of(
                        "eventId", event.getId().toString(),
                        "eventType", event.getEventType(),
                        "aggregateId", event.getAggregateId().toString(),
                        "occurredAt", event.getOccurredAt().toString(),
                        "payload", event.getPayload()
                )).withStreamKey(stream));
                event.markDispatched();
            } catch (Exception ex) {
                event.recordAttempt();
                log.warn("Outbox relay failed for {} (attempt {})", event.getId(),
                        event.getAttempts(), ex);
            }
        }
        repository.saveAll(batch);
    }
}
