package com.santimpay.posctl.health.application;

import java.time.Instant;
import java.util.UUID;

/** One telemetry sample to ingest. {@code deviceId} may be null if only the serial is known. */
public record IngestHealthCommand(
        UUID deviceId,
        String serialNo,
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
        Instant reportedAt) {}
