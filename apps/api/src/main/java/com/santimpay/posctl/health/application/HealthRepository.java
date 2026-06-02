package com.santimpay.posctl.health.application;

import com.santimpay.posctl.health.domain.DeviceHealthReport;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HealthRepository {

    DeviceHealthReport save(DeviceHealthReport report);

    List<DeviceHealthReport> saveAll(List<DeviceHealthReport> reports);

    /** Latest sample for a device (for the snapshot endpoint / offline detection). */
    Optional<DeviceHealthReport> findLatestByDevice(UUID deviceId);
}
