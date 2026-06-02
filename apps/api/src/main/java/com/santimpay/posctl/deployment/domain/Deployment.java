package com.santimpay.posctl.deployment.domain;

import com.santimpay.posctl.deployment.events.DeploymentCompleted;
import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;

/**
 * Daily deployment record. References merchant/branch/device/agent by id (cross-context, no imports).
 * The act of completing a deployment is what produces a current {@link DeviceAssignment} and the
 * {@link DeploymentCompleted} event.
 */
@Getter
@Entity
@Table(name = "deployments", schema = "deployment")
public class Deployment extends AggregateRoot<Deployment> {

    @Column(name = "deployment_no", nullable = false, unique = true, updatable = false)
    private String deploymentNo;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "device_id")
    private UUID deviceId;

    @Column(name = "assigned_agent")
    private UUID assignedAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeploymentStatus status;

    @Column(name = "received_by")
    private String receivedBy;

    @Column(name = "trello_card_id")
    private String trelloCardId;

    @Column(name = "conversation_notes")
    private String conversationNotes;

    @Column(name = "gps_latitude")
    private Double gpsLatitude;

    @Column(name = "gps_longitude")
    private Double gpsLongitude;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected Deployment() {}

    public static Deployment plan(String deploymentNo, LocalDate scheduledDate,
                                  UUID merchantId, UUID branchId, UUID assignedAgent) {
        if (merchantId == null || branchId == null) {
            throw DomainException.invalidState("merchantId and branchId are required");
        }
        Deployment d = new Deployment();
        d.assignIdentityIfAbsent();
        d.deploymentNo = deploymentNo;
        d.scheduledDate = scheduledDate;
        d.merchantId = merchantId;
        d.branchId = branchId;
        d.assignedAgent = assignedAgent;
        d.status = DeploymentStatus.PLANNED;
        return d;
    }

    public void start() {
        if (status != DeploymentStatus.PLANNED) {
            throw DomainException.conflict("Only a PLANNED deployment can be started");
        }
        this.status = DeploymentStatus.IN_PROGRESS;
    }

    /** Complete the deployment with field evidence and bind the device. Raises DeploymentCompleted. */
    public void complete(UUID deviceId, String receivedBy, Double lat, Double lng,
                         String conversationNotes, String trelloCardId) {
        if (status == DeploymentStatus.COMPLETED) {
            throw DomainException.conflict("Deployment already completed");
        }
        if (deviceId == null) {
            throw DomainException.invalidState("deviceId is required to complete a deployment");
        }
        this.deviceId = deviceId;
        this.receivedBy = receivedBy;
        this.gpsLatitude = lat;
        this.gpsLongitude = lng;
        this.conversationNotes = conversationNotes;
        this.trelloCardId = trelloCardId;
        this.status = DeploymentStatus.COMPLETED;
        this.completedAt = Instant.now();
        raise(new DeploymentCompleted(getId(), merchantId, branchId, deviceId, Instant.now()));
    }

    public void fail(String reason) {
        this.status = DeploymentStatus.FAILED;
    }
}
