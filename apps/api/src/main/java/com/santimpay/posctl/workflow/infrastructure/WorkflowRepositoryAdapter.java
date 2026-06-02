package com.santimpay.posctl.workflow.infrastructure;

import com.santimpay.posctl.workflow.application.WorkflowRepository;
import com.santimpay.posctl.workflow.domain.WorkflowApproval;
import com.santimpay.posctl.workflow.domain.WorkflowInstance;
import com.santimpay.posctl.workflow.domain.WorkflowStatus;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class WorkflowRepositoryAdapter implements WorkflowRepository {

    private final WorkflowJpaRepository instanceJpa;
    private final WorkflowApprovalJpaRepository approvalJpa;

    @Override
    public WorkflowInstance save(WorkflowInstance instance) {
        return instanceJpa.save(instance);
    }

    @Override
    public WorkflowApproval save(WorkflowApproval approval) {
        return approvalJpa.save(approval);
    }

    @Override
    public Optional<WorkflowInstance> findById(UUID id) {
        return instanceJpa.findById(id);
    }

    @Override
    public Page<WorkflowInstance> search(WorkflowStatus status, String subjectType, Pageable pageable) {
        return instanceJpa.search(status, subjectType, pageable);
    }
}
