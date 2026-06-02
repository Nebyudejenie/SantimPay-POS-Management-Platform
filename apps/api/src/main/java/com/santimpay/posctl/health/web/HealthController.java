package com.santimpay.posctl.health.web;

import com.santimpay.posctl.health.application.HealthService;
import com.santimpay.posctl.health.application.IngestHealthCommand;
import com.santimpay.posctl.health.domain.DeviceHealthReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Device telemetry ingest + snapshot. Ingest endpoints are high-volume and accept a single sample or
 * a batch from a collector; they require {@code device:telemetry} (see SecurityConfig — these are the
 * only POSTs not behind a generic authenticated rule).
 */
@Tag(name = "Device Health")
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService service;

    public record HealthReportRequest(
            UUID deviceId,
            @NotBlank String serialNo,
            String deviceStatus,
            Integer batteryLevel,
            String mobileData,
            String ipAddress,
            Double latitude,
            Double longitude,
            Double cpuUsage,
            Integer ramAvailableMb,
            Integer storageAvailableMb,
            Integer signalStrength,
            String appVersion,
            String osVersion,
            Instant reportedAt) {

        IngestHealthCommand toCommand() {
            return new IngestHealthCommand(deviceId, serialNo, deviceStatus, batteryLevel, mobileData,
                    ipAddress, latitude, longitude, cpuUsage, ramAvailableMb, storageAvailableMb,
                    signalStrength, appVersion, osVersion, reportedAt);
        }
    }

    public record LatestHealthResponse(UUID deviceId, String serialNo, String deviceStatus,
                                       Integer batteryLevel, Integer signalStrength, Double latitude,
                                       Double longitude, Instant reportedAt) {}

    @Operation(summary = "Ingest a single device telemetry sample")
    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ingest(@Valid @RequestBody HealthReportRequest req) {
        service.ingest(req.toCommand());
    }

    @Operation(summary = "Bulk-ingest telemetry samples from a collector")
    @PostMapping("/reports:bulk")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BulkResult ingestBulk(@Valid @RequestBody List<HealthReportRequest> reqs) {
        int n = service.ingestBulk(reqs.stream().map(HealthReportRequest::toCommand).toList());
        return new BulkResult(n);
    }

    public record BulkResult(int accepted) {}

    @Operation(summary = "Latest health snapshot for a device")
    @GetMapping("/devices/{id}/latest")
    public LatestHealthResponse latest(@PathVariable UUID id) {
        DeviceHealthReport r = service.latest(id);
        return new LatestHealthResponse(r.getDeviceId(), r.getSerialNo(), r.getDeviceStatus(),
                r.getBatteryLevel(), r.getSignalStrength(), r.getLatitude(), r.getLongitude(),
                r.getReportedAt());
    }
}
