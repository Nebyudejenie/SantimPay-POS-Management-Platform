package com.santimpay.posctl.workflow.infrastructure;

import com.santimpay.posctl.workflow.domain.WorkflowApproval;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface WorkflowApprovalJpaRepository extends JpaRepository<WorkflowApproval, UUID> {
}
