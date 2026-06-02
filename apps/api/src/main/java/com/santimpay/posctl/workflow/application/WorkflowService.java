package com.santimpay.posctl.workflow.application;

import com.santimpay.posctl.shared.domain.DomainException;
import com.santimpay.posctl.shared.security.CurrentUser;
import com.santimpay.posctl.workflow.domain.WorkflowApproval;
import com.santimpay.posctl.workflow.domain.WorkflowInstance;
import com.santimpay.posctl.workflow.domain.WorkflowStatus;
import com.santimpay.posctl.workflow.domain.WorkflowType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generic approval engine. Implements {@link WorkflowInitiation} (callable by other modules) and the
 * decide use case (driven by an authorized human). Decisions enforce maker≠checker in the aggregate
 * and are recorded as immutable {@link WorkflowApproval} rows.
 */
@Service
@RequiredArgsConstructor
public class WorkflowService implements WorkflowInitiation {

    private final WorkflowRepository repository;

    @Override
    @Transactional
    public UUID start(String type, String subjectType, UUID subjectId, UUID initiatedBy) {
        WorkflowType workflowType;
        try {
            workflowType = WorkflowType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw DomainException.invalidState("Unknown workflow type: " + type);
        }
        WorkflowInstance instance =
                WorkflowInstance.initiate(workflowType, subjectType, subjectId, initiatedBy);
        return repository.save(instance).getId();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_workflow:read')")
    public WorkflowInstance get(UUID id) {
        return repository.findById(id).orElseThrow(() -> DomainException.notFound("Workflow", id));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_workflow:read')")
    public Page<WorkflowInstance> search(WorkflowStatus status, String subjectType, Pageable pageable) {
        return repository.search(status, subjectType, pageable);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_workflow:approve')")
    public WorkflowInstance approve(UUID id, String comment) {
        UUID approver = CurrentUser.id().orElse(null);
        WorkflowInstance instance = get(id);
        instance.approve(approver);
        repository.save(WorkflowApproval.record(id, instance.getCurrentStep(), approver, "approved", comment));
        return repository.save(instance);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_workflow:approve')")
    public WorkflowInstance reject(UUID id, String reason) {
        UUID approver = CurrentUser.id().orElse(null);
        WorkflowInstance instance = get(id);
        instance.reject(approver, reason);
        repository.save(WorkflowApproval.record(id, instance.getCurrentStep(), approver, "rejected", reason));
        return repository.save(instance);
    }
}
