package com.santimpay.posctl.workflow.application;

import com.santimpay.posctl.workflow.domain.WorkflowApproval;
import com.santimpay.posctl.workflow.domain.WorkflowInstance;
import com.santimpay.posctl.workflow.domain.WorkflowStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkflowRepository {

    WorkflowInstance save(WorkflowInstance instance);

    WorkflowApproval save(WorkflowApproval approval);

    Optional<WorkflowInstance> findById(UUID id);

    Page<WorkflowInstance> search(WorkflowStatus status, String subjectType, Pageable pageable);
}
