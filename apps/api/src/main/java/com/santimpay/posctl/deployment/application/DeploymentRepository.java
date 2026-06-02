package com.santimpay.posctl.deployment.application;

import com.santimpay.posctl.deployment.domain.Deployment;
import com.santimpay.posctl.deployment.domain.DeploymentStatus;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeploymentRepository {

    Deployment save(Deployment deployment);

    Optional<Deployment> findById(UUID id);

    Page<Deployment> search(LocalDate date, DeploymentStatus status, UUID agentId, Pageable pageable);
}
