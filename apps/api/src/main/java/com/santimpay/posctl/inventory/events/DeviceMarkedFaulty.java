package com.santimpay.posctl.inventory.events;

import com.santimpay.posctl.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Published when a deployed device is reported faulty. Consumed by deployment (it may need a swap),
 * tasks (auto-create an RMA/swap task) and ai (failure-prediction signal).
 */
public record DeviceMarkedFaulty(
        UUID deviceId,
        String serialNo,
        String reason,
        Instant occurredAt) implements DomainEvent {

    @Override public UUID aggregateId() { return deviceId; }
    @Override public String aggregateType() { return "device"; }
}
