package com.santimpay.posctl.deployment.infrastructure;

import com.santimpay.posctl.deployment.application.DeploymentRepository;
import com.santimpay.posctl.deployment.application.DeviceAssignmentRepository;
import com.santimpay.posctl.deployment.domain.Deployment;
import com.santimpay.posctl.deployment.domain.DeploymentStatus;
import com.santimpay.posctl.deployment.domain.DeviceAssignment;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/** Adapters for both deployment aggregate ports over Spring Data JPA. */
@Component
@RequiredArgsConstructor
class DeploymentRepositoryAdapter implements DeploymentRepository, DeviceAssignmentRepository {

    private final DeploymentJpaRepository deploymentJpa;
    private final DeviceAssignmentJpaRepository assignmentJpa;

    @Override
    public Deployment save(Deployment deployment) {
        return deploymentJpa.save(deployment);
    }

    @Override
    public Optional<Deployment> findById(UUID id) {
        return deploymentJpa.findById(id).filter(d -> !d.getAudit().isDeleted());
    }

    @Override
    public Page<Deployment> search(LocalDate date, DeploymentStatus status, UUID agentId, Pageable pageable) {
        return deploymentJpa.search(date, status, agentId, pageable);
    }

    @Override
    public DeviceAssignment save(DeviceAssignment assignment) {
        return assignmentJpa.save(assignment);
    }

    @Override
    public Optional<DeviceAssignment> findCurrentByDevice(UUID deviceId) {
        return assignmentJpa.findByDeviceIdAndCurrentTrue(deviceId);
    }
}
