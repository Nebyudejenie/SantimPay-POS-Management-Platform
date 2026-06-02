package com.santimpay.posctl.deployment.events;

import com.santimpay.posctl.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Published when a device becomes the current assignment at a branch. Consumed by inventory (move
 * the device to DEPLOYED) and analytics. This is the seam that lets deployment drive inventory
 * WITHOUT importing it — inventory reacts to the fact, decoupled and retryable.
 */
public record DeviceAssigned(
        UUID assignmentId,
        UUID deviceId,
        UUID branchId,
        UUID merchantId,
        UUID deploymentId,
        Instant occurredAt) implements DomainEvent {

    @Override public UUID aggregateId() { return assignmentId; }
    @Override public String aggregateType() { return "device_assignment"; }
}
