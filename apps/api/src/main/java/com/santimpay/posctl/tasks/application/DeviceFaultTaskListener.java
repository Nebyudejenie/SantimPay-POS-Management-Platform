package com.santimpay.posctl.tasks.application;

import com.santimpay.posctl.inventory.events.DeviceMarkedFaulty;
import com.santimpay.posctl.tasks.domain.TaskPriority;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Auto-creates a swap/RMA task when a deployed device is marked faulty. Demonstrates event-driven
 * work generation: tasks reacts to inventory's published {@code DeviceMarkedFaulty} and spawns a
 * HIGH-priority task related to the device — no coupling beyond the event type.
 */
@Component
@RequiredArgsConstructor
public class DeviceFaultTaskListener {

    private final TaskCreation tasks;

    @ApplicationModuleListener
    public void on(DeviceMarkedFaulty event) {
        tasks.createSystemTask(
                "Swap faulty POS device " + event.serialNo(),
                "Device reported faulty: " + event.reason() + ". Arrange RMA / replacement.",
                "device_swap",
                TaskPriority.HIGH,
                "device",
                event.deviceId());
    }
}
