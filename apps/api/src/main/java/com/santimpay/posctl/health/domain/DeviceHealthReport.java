package com.santimpay.posctl.health.domain;

import com.santimpay.posctl.shared.domain.UuidV7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/**
 * A single device telemetry sample (the "POS Health / Device Information" report). This is the
 * highest-volume table in the system (potentially millions of rows/day), so it is deliberately
 * <b>NOT</b> a DDD aggregate: it is an append-only, immutable fact with no behavior, no audit trigger
 * (auditing would double an already huge write rate), and no soft-delete/version columns. It maps to
 * a monthly range-partitioned table. Raw rows are kept 90 days hot, then rolled up + archived.
 *
 * <p>Uses a plain assigned UUIDv7 id (no {@code AggregateRoot}) — time-ordered for partition/index
 * locality.
 */
@Getter
@Entity
@Table(name = "device_health_reports", schema = "health")
public class DeviceHealthReport {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "reported_at", nullable = false, updatable = false)
    private Instant reportedAt;

    @Column(name = "device_id")
    private UUID deviceId;

    @Column(name = "serial_no", nullable = false, updatable = false)
    private String serialNo;

    @Column(name = "device_status")
    private String deviceStatus;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "mobile_data")
    private String mobileData;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "cpu_usage")
    private Double cpuUsage;

    @Column(name = "ram_available_mb")
    private Integer ramAvailableMb;

    @Column(name = "storage_available_mb")
    private Integer storageAvailableMb;

    @Column(name = "signal_strength")
    private Integer signalStrength;

    @Column(name = "app_version")
    private String appVersion;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    protected DeviceHealthReport() {}

    public static DeviceHealthReport of(UUID deviceId, String serialNo, String deviceStatus,
                                        Integer batteryLevel, String mobileData, String ipAddress,
                                        Double latitude, Double longitude, Double cpuUsage,
                                        Integer ramAvailableMb, Integer storageAvailableMb,
                                        Integer signalStrength, String appVersion, String osVersion,
                                        Instant reportedAt) {
        DeviceHealthReport r = new DeviceHealthReport();
        r.id = UuidV7.generate();
        r.deviceId = deviceId;
        r.serialNo = serialNo;
        r.deviceStatus = deviceStatus;
        r.batteryLevel = batteryLevel;
        r.mobileData = mobileData;
        r.ipAddress = ipAddress;
        r.latitude = latitude;
        r.longitude = longitude;
        r.cpuUsage = cpuUsage;
        r.ramAvailableMb = ramAvailableMb;
        r.storageAvailableMb = storageAvailableMb;
        r.signalStrength = signalStrength;
        r.appVersion = appVersion;
        r.osVersion = osVersion;
        r.reportedAt = reportedAt == null ? Instant.now() : reportedAt;
        r.lastSyncAt = Instant.now();
        return r;
    }

    public boolean indicatesOffline() {
        return "offline".equalsIgnoreCase(deviceStatus);
    }
}
