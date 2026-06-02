package com.santimpay.posctl.workflow.events;

import com.santimpay.posctl.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Published when a workflow instance is fully approved. The subject-owning module listens for its
 * {@code subjectType} and performs the gated action (e.g. merchant activation).
 */
public record ApprovalGranted(
        UUID instanceId,
        String workflowType,
        String subjectType,
        UUID subjectId,
        UUID approvedBy,
        Instant occurredAt) implements DomainEvent {

    @Override public UUID aggregateId() { return instanceId; }
    @Override public String aggregateType() { return "workflow"; }
}
