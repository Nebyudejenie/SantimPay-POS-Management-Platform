package com.santimpay.posctl.inventory.web;

import com.santimpay.posctl.inventory.domain.DeviceMaintenance;
import com.santimpay.posctl.inventory.domain.SimAllocationHistory;
import com.santimpay.posctl.inventory.infrastructure.DeviceMaintenanceJpaRepository;
import com.santimpay.posctl.inventory.infrastructure.SimAllocationHistoryJpaRepository;
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

/** Device details — maintenance history, SIM allocation, activity. */
@Tag(name = "Device Details")
@RestController
@RequestMapping("/api/v1/devices/{deviceId}")
@RequiredArgsConstructor
public class DeviceDetailController {

    private final DeviceMaintenanceJpaRepository maintenance;
    private final SimAllocationHistoryJpaRepository simHistory;

    // ========== MAINTENANCE ==========
    public record MaintenanceResponse(UUID id, String maintenanceType, String issueDescription, String resolution, Instant completedAt) {}
    public record CreateMaintenanceRequest(@NotBlank String maintenanceType, String issueDescription) {}

    @Operation(summary = "View device maintenance history")
    @GetMapping("/maintenance")
    @PreAuthorize("hasAuthority('PERM_device:read')")
    public List<MaintenanceResponse> listMaintenance(@PathVariable UUID deviceId) {
        return maintenance.findByDevice(deviceId).stream()
                .map(m -> new MaintenanceResponse(m.getId(), m.getMaintenanceType(), m.getIssueDescription(),
                        m.getResolution(), m.getCompletionDate()))
                .toList();
    }

    @Operation(summary = "Create maintenance ticket for device")
    @PostMapping("/maintenance")
    @PreAuthorize("hasAuthority('PERM_device:update')")
    @Transactional
    public ResponseEntity<MaintenanceResponse> createMaintenance(@PathVariable UUID deviceId,
                                                                @RequestBody CreateMaintenanceRequest req) {
        DeviceMaintenance m = maintenance.save(DeviceMaintenance.initiate(deviceId, req.maintenanceType(), req.issueDescription()));
        return ResponseEntity.status(201).body(new MaintenanceResponse(m.getId(), m.getMaintenanceType(),
                m.getIssueDescription(), null, null));
    }

    @Operation(summary = "Mark maintenance as complete")
    @PostMapping("/maintenance/{maintenanceId}:complete")
    @PreAuthorize("hasAuthority('PERM_device:update')")
    @Transactional
    public ResponseEntity<Void> completeMaintenance(@PathVariable UUID deviceId, @PathVariable UUID maintenanceId,
                                                   @RequestParam @NotBlank String resolution) {
        maintenance.findById(maintenanceId).ifPresent(m -> {
            m.complete(resolution, null);
            maintenance.save(m);
        });
        return ResponseEntity.ok().build();
    }

    // ========== SIM ALLOCATION ==========
    public record SimResponse(UUID id, UUID simId, Instant allocatedAt, Instant deallocatedAt, boolean isCurrent) {}
    public record AllocateSimRequest(@NotBlank String simId) {}

    @Operation(summary = "View SIM allocation history for device")
    @GetMapping("/sims")
    @PreAuthorize("hasAuthority('PERM_device:read')")
    public List<SimResponse> listSims(@PathVariable UUID deviceId) {
        return simHistory.findByDevice(deviceId).stream()
                .map(h -> new SimResponse(h.getId(), h.getSimId(), h.getAllocatedAt(), h.getDeallocatedAt(), h.isActive()))
                .toList();
    }

    @Operation(summary = "Allocate a SIM card to device")
    @PostMapping("/sims:allocate")
    @PreAuthorize("hasAuthority('PERM_device:update')")
    @Transactional
    public ResponseEntity<SimResponse> allocateSim(@PathVariable UUID deviceId,
                                                  @RequestBody AllocateSimRequest req) {
        UUID simId = UUID.fromString(req.simId());
        SimAllocationHistory h = simHistory.save(SimAllocationHistory.allocate(deviceId, simId, "allocation"));
        return ResponseEntity.status(201).body(new SimResponse(h.getId(), h.getSimId(), h.getAllocatedAt(), null, true));
    }

    @Operation(summary = "Deallocate current SIM from device")
    @PostMapping("/sims:deallocate")
    @PreAuthorize("hasAuthority('PERM_device:update')")
    @Transactional
    public ResponseEntity<Void> deallocateSim(@PathVariable UUID deviceId) {
        SimAllocationHistory h = simHistory.findCurrentByDevice(deviceId);
        if (h != null) {
            h.deallocate();
            simHistory.save(h);
        }
        return ResponseEntity.ok().build();
    }
}
