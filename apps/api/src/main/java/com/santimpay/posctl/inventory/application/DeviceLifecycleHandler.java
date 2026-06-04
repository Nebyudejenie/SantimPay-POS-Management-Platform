package com.santimpay.posctl.inventory.application;

import com.santimpay.posctl.inventory.domain.DeviceStatus;
import com.santimpay.posctl.inventory.domain.PosDevice;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Internal (no {@code @PreAuthorize}) lifecycle operations driven by domain events rather than a
 * user action — event handlers run in a system context with no authenticated principal, so they must
 * not go through method-security-guarded use cases. Kept package-internal to inventory.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceLifecycleHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeviceLifecycleHandler.class);

    private final DeviceRepository repository;

    /** Move a device to DEPLOYED when it gets assigned to a branch in the field. Idempotent. */
    @Transactional
    public void onAssignedToBranch(UUID deviceId) {
        PosDevice device = repository.findById(deviceId).orElse(null);
        if (device == null) {
            logger.warn("DeviceAssigned for unknown device {} — ignoring", deviceId);
            return;
        }
        if (device.getStatus() == DeviceStatus.DEPLOYED) {
            return; // already there; idempotent replay
        }
        if (device.getStatus() == DeviceStatus.IN_STOCK) {
            device.allocate();
        }
        device.markDeployed();
        repository.save(device);
        logger.info("Device {} moved to DEPLOYED via assignment", deviceId);
    }
}
