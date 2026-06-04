package com.santimpay.posctl.deployment.web;

import com.santimpay.posctl.deployment.domain.DeploymentEvent;
import com.santimpay.posctl.deployment.infrastructure.DeploymentEventRepositories.DeploymentEventJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/** Deployment event timeline — track progress from assignment to completion. */
@Tag(name = "Deployment Details")
@RestController
@RequestMapping("/api/v1/deployments/{deploymentId}")
@RequiredArgsConstructor
public class DeploymentDetailController {

    private final DeploymentEventJpaRepository events;

    public record EventResponse(UUID id, String eventType, String eventStatus, Instant timestamp, String description, String photoUrl, Double lat, Double lon) {}
    public record CreateEventRequest(@NotBlank String eventType, String description, String photoUrl, Double latitude, Double longitude) {}

    @Operation(summary = "View deployment event timeline")
    @GetMapping("/events")
    @PreAuthorize("hasAuthority('PERM_deployment:read')")
    public List<EventResponse> listEvents(@PathVariable UUID deploymentId) {
        return events.findByDeployment(deploymentId).stream()
                .map(e -> new EventResponse(e.getId(), e.getEventType(), e.getEventStatus(), e.getEventTimestamp(),
                        e.getDescription(), e.getPhotoUrl(), e.getGpsLatitude(), e.getGpsLongitude()))
                .toList();
    }

    @Operation(summary = "Record a deployment event (arrived, setup complete, signed, etc.)")
    @PostMapping("/events")
    @PreAuthorize("hasAuthority('PERM_deployment:events')")
    @Transactional
    public ResponseEntity<EventResponse> recordEvent(@PathVariable UUID deploymentId,
                                                    @RequestBody CreateEventRequest req) {
        DeploymentEvent e = events.save(DeploymentEvent.create(deploymentId, req.eventType(), "COMPLETED"));
        if (req.description() != null) e.withRemarks(req.description());
        if (req.photoUrl() != null) e.withPhoto(req.photoUrl());
        if (req.latitude() != null && req.longitude() != null) e.withLocation(req.latitude(), req.longitude());
        events.save(e);
        return ResponseEntity.status(201).body(new EventResponse(e.getId(), e.getEventType(), e.getEventStatus(),
                e.getEventTimestamp(), e.getRemarks(), e.getPhotoUrl(), e.getGpsLatitude(), e.getGpsLongitude()));
    }
}
