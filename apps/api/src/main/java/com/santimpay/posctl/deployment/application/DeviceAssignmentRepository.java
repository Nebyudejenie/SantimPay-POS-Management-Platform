package com.santimpay.posctl.deployment.application;

import com.santimpay.posctl.deployment.domain.DeviceAssignment;
import java.util.Optional;
import java.util.UUID;

public interface DeviceAssignmentRepository {

    DeviceAssignment save(DeviceAssignment assignment);

    /** The open assignment for a device, if any (is_current = true). */
    Optional<DeviceAssignment> findCurrentByDevice(UUID deviceId);
}
