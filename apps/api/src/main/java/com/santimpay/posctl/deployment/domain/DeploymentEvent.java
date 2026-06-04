package com.santimpay.posctl.deployment.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;

/** Timeline event in a deployment — tracks progress from assignment through completion. */
@Getter
@Entity
@Table(name = "deployment_events", schema = "deployment")
public class DeploymentEvent extends AggregateRoot<DeploymentEvent> {

    @Column(name = "deployment_id", nullable = false, updatable = false)
    private UUID deploymentId;

    @Column(name = "event_type", nullable = false)
    private String eventType;  // assigned, agent_departed, arrived, demo_started, setup_complete, signature_collected, completed

    @Column(name = "event_status", nullable = false)
    private String eventStatus;  // PENDING, IN_PROGRESS, COMPLETED, FAILED

    @Column(name = "event_timestamp", nullable = false)
    private java.time.Instant eventTimestamp;

    @Column(name = "agent_id")
    private UUID agentId;

    @Column(name = "description")
    private String description;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "gps_latitude")
    private Double gpsLatitude;

    @Column(name = "gps_longitude")
    private Double gpsLongitude;

    @Column(name = "remarks")
    private String remarks;

    protected DeploymentEvent() {}

    public static DeploymentEvent create(UUID deploymentId, String eventType, String eventStatus) {
        if (deploymentId == null || eventType == null) {
            throw DomainException.invalidState("deploymentId and eventType required");
        }
        DeploymentEvent e = new DeploymentEvent();
        e.assignIdentityIfAbsent();
        e.deploymentId = deploymentId;
        e.eventType = eventType;
        e.eventStatus = eventStatus != null ? eventStatus : "COMPLETED";
        e.eventTimestamp = java.time.Instant.now();
        return e;
    }

    public void withPhoto(String photoUrl) { this.photoUrl = photoUrl; }
    public void withLocation(Double lat, Double lon) { this.gpsLatitude = lat; this.gpsLongitude = lon; }
    public void withRemarks(String remarks) { this.remarks = remarks; }
    public void withAgent(UUID agentId) { this.agentId = agentId; }
}
