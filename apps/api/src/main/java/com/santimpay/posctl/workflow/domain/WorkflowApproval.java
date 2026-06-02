package com.santimpay.posctl.workflow.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/** Immutable record of one approval decision against a workflow instance (audit of who decided). */
@Getter
@Entity
@Table(name = "workflow_approvals", schema = "workflow")
public class WorkflowApproval extends AggregateRoot<WorkflowApproval> {

    @Column(name = "instance_id", nullable = false, updatable = false)
    private UUID instanceId;

    @Column(name = "step_no", nullable = false, updatable = false)
    private int stepNo;

    @Column(name = "approver_id", updatable = false)
    private UUID approverId;

    @Column(name = "decision", nullable = false, updatable = false)
    private String decision;

    @Column(name = "comment")
    private String comment;

    @Column(name = "decided_at", nullable = false, updatable = false)
    private Instant decidedAt;

    protected WorkflowApproval() {}

    public static WorkflowApproval record(UUID instanceId, int stepNo, UUID approverId,
                                          String decision, String comment) {
        WorkflowApproval a = new WorkflowApproval();
        a.assignIdentityIfAbsent();
        a.instanceId = instanceId;
        a.stepNo = stepNo;
        a.approverId = approverId;
        a.decision = decision;
        a.comment = comment;
        a.decidedAt = Instant.now();
        return a;
    }
}
