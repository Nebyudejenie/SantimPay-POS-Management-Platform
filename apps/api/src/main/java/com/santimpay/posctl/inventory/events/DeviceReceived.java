package com.santimpay.posctl.inventory.events;

import com.santimpay.posctl.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/** Published when a new device enters stock. Consumed by analytics. */
public record DeviceReceived(
        UUID deviceId,
        String serialNo,
        String model,
        Instant occurredAt) implements DomainEvent {

    @Override public UUID aggregateId() { return deviceId; }
    @Override public String aggregateType() { return "device"; }
}
