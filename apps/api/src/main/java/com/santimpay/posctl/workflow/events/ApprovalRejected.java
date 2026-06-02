package com.santimpay.posctl.workflow.events;

import com.santimpay.posctl.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record ApprovalRejected(
        UUID instanceId,
        String workflowType,
        String subjectType,
        UUID subjectId,
        UUID rejectedBy,
        String reason,
        Instant occurredAt) implements DomainEvent {

    @Override public UUID aggregateId() { return instanceId; }
    @Override public String aggregateType() { return "workflow"; }
}
