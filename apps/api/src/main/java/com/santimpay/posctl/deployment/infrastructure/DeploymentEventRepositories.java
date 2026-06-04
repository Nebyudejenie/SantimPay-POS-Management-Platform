package com.santimpay.posctl.deployment.infrastructure;

import com.santimpay.posctl.deployment.domain.DeploymentEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface DeploymentEventJpaRepository extends JpaRepository<DeploymentEvent, UUID> {
    @Query("select e from DeploymentEvent e where e.deploymentId = :depId and e.audit.deletedAt is null order by e.eventTimestamp asc")
    List<DeploymentEvent> findByDeployment(@Param("depId") UUID deploymentId);

    @Query("select e from DeploymentEvent e where e.deploymentId = :depId and e.eventType = :type and e.audit.deletedAt is null")
    List<DeploymentEvent> findByDeploymentAndType(@Param("depId") UUID deploymentId, @Param("type") String eventType);
}
