package com.santimpay.posctl.deployment.infrastructure;

import com.santimpay.posctl.deployment.domain.Deployment;
import com.santimpay.posctl.deployment.domain.DeploymentStatus;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface DeploymentJpaRepository extends JpaRepository<Deployment, UUID> {

    @Query("""
           select d from Deployment d
           where d.audit.deletedAt is null
             and (:date is null or d.scheduledDate = :date)
             and (:status is null or d.status = :status)
             and (:agentId is null or d.assignedAgent = :agentId)
           """)
    Page<Deployment> search(@Param("date") LocalDate date,
                            @Param("status") DeploymentStatus status,
                            @Param("agentId") UUID agentId,
                            Pageable pageable);
}
