package com.santimpay.posctl.workflow.infrastructure;

import com.santimpay.posctl.workflow.domain.WorkflowInstance;
import com.santimpay.posctl.workflow.domain.WorkflowStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface WorkflowJpaRepository extends JpaRepository<WorkflowInstance, UUID> {

    @Query("""
           select w from WorkflowInstance w
           where (:status is null or w.status = :status)
             and (:subjectType is null or w.subjectType = :subjectType)
           """)
    Page<WorkflowInstance> search(@Param("status") WorkflowStatus status,
                                  @Param("subjectType") String subjectType,
                                  Pageable pageable);
}
