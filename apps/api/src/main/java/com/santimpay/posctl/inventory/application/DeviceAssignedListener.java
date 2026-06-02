package com.santimpay.posctl.inventory.application;

import com.santimpay.posctl.deployment.events.DeviceAssigned;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Inventory reacts to a field assignment by advancing the device's lifecycle to DEPLOYED. The only
 * coupling to deployment is its published {@link DeviceAssigned} event — inventory never imports
 * deployment's domain/services. {@code @ApplicationModuleListener} makes this transactional, async,
 * and tracked by the event-publication registry (retried on failure, never lost).
 */
@Component
@RequiredArgsConstructor
public class DeviceAssignedListener {

    private final DeviceLifecycleHandler lifecycle;

    @ApplicationModuleListener
    public void on(DeviceAssigned event) {
        lifecycle.onAssignedToBranch(event.deviceId());
    }
}
