package com.santimpay.posctl.tasks.events;

import com.santimpay.posctl.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record TaskCompleted(
        UUID taskId,
        UUID assigneeId,
        Instant occurredAt) implements DomainEvent {

    @Override public UUID aggregateId() { return taskId; }
    @Override public String aggregateType() { return "task"; }
}
