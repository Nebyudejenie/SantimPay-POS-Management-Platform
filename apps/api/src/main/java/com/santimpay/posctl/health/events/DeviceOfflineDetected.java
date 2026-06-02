package com.santimpay.posctl.health.events;

import com.santimpay.posctl.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Published when a device transitions to offline based on incoming telemetry. Consumed by tasks
 * (open a check-up task), followup (schedule an outreach) and notifications.
 */
public record DeviceOfflineDetected(
        UUID deviceId,
        String serialNo,
        Instant lastSeenAt,
        Instant occurredAt) implements DomainEvent {

    @Override public UUID aggregateId() { return deviceId == null ? new UUID(0, 0) : deviceId; }
    @Override public String aggregateType() { return "device_health"; }
}
