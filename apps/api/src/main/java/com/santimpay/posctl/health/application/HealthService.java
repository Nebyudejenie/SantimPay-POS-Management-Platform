package com.santimpay.posctl.health.application;

import com.santimpay.posctl.health.domain.DeviceHealthReport;
import com.santimpay.posctl.health.events.DeviceOfflineDetected;
import com.santimpay.posctl.shared.domain.DomainException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Telemetry ingest. Optimized for write throughput, not CRUD: append-only inserts, bulk path for the
 * collector, and offline detection that publishes {@link DeviceOfflineDetected} only on the
 * transition (previous sample online → this one offline) to avoid event storms.
 *
 * <p>Authorized with {@code device:telemetry} (granted to the field role + the device service
 * account). Not method-secured on the internal map helper.
 */
@Service
@RequiredArgsConstructor
public class HealthService {

    private final HealthRepository repository;
    private final ApplicationEventPublisher events;

    @Transactional
    @PreAuthorize("hasAuthority('PERM_device:telemetry')")
    public void ingest(IngestHealthCommand cmd) {
        DeviceHealthReport prev = cmd.deviceId() == null ? null
                : repository.findLatestByDevice(cmd.deviceId()).orElse(null);
        DeviceHealthReport saved = repository.save(toEntity(cmd));
        detectOfflineTransition(prev, saved);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_device:telemetry')")
    public int ingestBulk(List<IngestHealthCommand> batch) {
        if (batch == null || batch.isEmpty()) return 0;
        List<DeviceHealthReport> saved = repository.saveAll(batch.stream().map(this::toEntity).toList());
        return saved.size();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_device:read')")
    public DeviceHealthReport latest(UUID deviceId) {
        return repository.findLatestByDevice(deviceId)
                .orElseThrow(() -> DomainException.notFound("DeviceHealthReport for device", deviceId));
    }

    private void detectOfflineTransition(DeviceHealthReport prev, DeviceHealthReport current) {
        boolean wasOnline = prev == null || !prev.indicatesOffline();
        if (current.getDeviceId() != null && current.indicatesOffline() && wasOnline) {
            Instant lastSeen = prev != null ? prev.getReportedAt() : current.getReportedAt();
            events.publishEvent(new DeviceOfflineDetected(
                    current.getDeviceId(), current.getSerialNo(), lastSeen, Instant.now()));
        }
    }

    private DeviceHealthReport toEntity(IngestHealthCommand c) {
        return DeviceHealthReport.of(c.deviceId(), c.serialNo(), c.deviceStatus(), c.batteryLevel(),
                c.mobileData(), c.ipAddress(), c.latitude(), c.longitude(), c.cpuUsage(),
                c.ramAvailableMb(), c.storageAvailableMb(), c.signalStrength(), c.appVersion(),
                c.osVersion(), c.reportedAt());
    }

    // expose Optional form for callers that prefer it
    public Optional<DeviceHealthReport> latestOrEmpty(UUID deviceId) {
        return repository.findLatestByDevice(deviceId);
    }
}
