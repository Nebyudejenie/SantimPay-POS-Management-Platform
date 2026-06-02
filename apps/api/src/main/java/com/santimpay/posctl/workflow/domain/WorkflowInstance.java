package com.santimpay.posctl.workflow.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import com.santimpay.posctl.workflow.events.ApprovalGranted;
import com.santimpay.posctl.workflow.events.ApprovalRejected;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/**
 * A pending approval over some subject (referenced by type + id, cross-context). Baseline supports
 * single-step approval with strict <b>segregation of duties</b>: the initiator (maker) may never be
 * the approver (checker). Multi-step routing extends {@code currentStep/totalSteps}.
 */
@Getter
@Entity
@Table(name = "workflow_instances", schema = "workflow")
public class WorkflowInstance extends AggregateRoot<WorkflowInstance> {

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_type", nullable = false, updatable = false)
    private WorkflowType workflowType;

    @Column(name = "subject_type", nullable = false, updatable = false)
    private String subjectType;

    @Column(name = "subject_id", nullable = false, updatable = false)
    private UUID subjectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkflowStatus status;

    @Column(name = "current_step", nullable = false)
    private int currentStep;

    @Column(name = "total_steps", nullable = false)
    private int totalSteps;

    @Column(name = "initiated_by")
    private UUID initiatedBy;

    protected WorkflowInstance() {}

    public static WorkflowInstance initiate(WorkflowType type, String subjectType, UUID subjectId,
                                            UUID initiatedBy) {
        WorkflowInstance w = new WorkflowInstance();
        w.assignIdentityIfAbsent();
        w.workflowType = type;
        w.subjectType = subjectType;
        w.subjectId = subjectId;
        w.initiatedBy = initiatedBy;
        w.status = WorkflowStatus.PENDING;
        w.currentStep = 1;
        w.totalSteps = 1;
        return w;
    }

    public void approve(UUID approverId) {
        guardDecidable(approverId);
        this.status = WorkflowStatus.APPROVED;
        raise(new ApprovalGranted(getId(), workflowType.name(), subjectType, subjectId,
                approverId, Instant.now()));
    }

    public void reject(UUID approverId, String reason) {
        guardDecidable(approverId);
        this.status = WorkflowStatus.REJECTED;
        raise(new ApprovalRejected(getId(), workflowType.name(), subjectType, subjectId,
                approverId, reason, Instant.now()));
    }

    private void guardDecidable(UUID approverId) {
        if (status != WorkflowStatus.PENDING) {
            throw DomainException.conflict("Workflow is not pending");
        }
        if (approverId != null && approverId.equals(initiatedBy)) {
            throw DomainException.conflict("Segregation of duties: the initiator cannot approve");
        }
    }
}
