package com.santimpay.posctl.deployment.application;

import com.santimpay.posctl.deployment.domain.Deployment;
import com.santimpay.posctl.deployment.domain.DeploymentStatus;
import com.santimpay.posctl.deployment.domain.DeviceAssignment;
import com.santimpay.posctl.shared.domain.DomainException;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deployment use cases. Completing a deployment is the orchestration point: it closes any prior
 * current assignment for the device (respecting the DB exclusion constraint), opens a new current
 * assignment (raising {@code DeviceAssigned} — inventory reacts), and completes the deployment
 * (raising {@code DeploymentCompleted}). All in one transaction; events flush to the outbox.
 */
@Service
@RequiredArgsConstructor
public class DeploymentService {

    private final DeploymentRepository deployments;
    private final DeviceAssignmentRepository assignments;

    @Transactional
    @PreAuthorize("hasAuthority('PERM_deployment:create')")
    public Deployment plan(PlanDeploymentCommand cmd) {
        return deployments.save(Deployment.plan(
                cmd.deploymentNo(), cmd.scheduledDate(), cmd.merchantId(),
                cmd.branchId(), cmd.assignedAgent()));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_deployment:read')")
    public Deployment get(UUID id) {
        return deployments.findById(id).orElseThrow(() -> DomainException.notFound("Deployment", id));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_deployment:read')")
    public Page<Deployment> search(LocalDate date, DeploymentStatus status, UUID agentId, Pageable pageable) {
        return deployments.search(date, status, agentId, pageable);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_deployment:complete')")
    public Deployment complete(CompleteDeploymentCommand cmd) {
        Deployment deployment = get(cmd.deploymentId());

        // Close prior current assignment for this device (swap/redeploy) before opening a new one,
        // so the device never has two overlapping assignments (the DB exclusion constraint backstops this).
        assignments.findCurrentByDevice(cmd.deviceId()).ifPresent(prev -> {
            prev.close();
            assignments.save(prev);
        });

        assignments.save(DeviceAssignment.open(
                cmd.deviceId(), deployment.getBranchId(), deployment.getMerchantId(), deployment.getId()));

        deployment.complete(cmd.deviceId(), cmd.receivedBy(), cmd.latitude(), cmd.longitude(),
                cmd.conversationNotes(), cmd.trelloCardId());
        return deployments.save(deployment);
    }
}
