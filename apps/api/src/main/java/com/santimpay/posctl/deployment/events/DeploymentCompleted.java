package com.santimpay.posctl.deployment.events;

import com.santimpay.posctl.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/** Published when a field deployment is completed. Consumed by tasks, analytics, notifications. */
public record DeploymentCompleted(
        UUID deploymentId,
        UUID merchantId,
        UUID branchId,
        UUID deviceId,
        Instant occurredAt) implements DomainEvent {

    @Override public UUID aggregateId() { return deploymentId; }
    @Override public String aggregateType() { return "deployment"; }
}
