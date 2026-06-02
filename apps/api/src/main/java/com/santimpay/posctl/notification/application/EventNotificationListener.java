package com.santimpay.posctl.notification.application;

import com.santimpay.posctl.deployment.events.DeploymentCompleted;
import com.santimpay.posctl.health.events.DeviceOfflineDetected;
import com.santimpay.posctl.inventory.events.DeviceMarkedFaulty;
import com.santimpay.posctl.workflow.events.ApprovalGranted;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Turns notable domain events into ops-inbox notifications (broadcast = null recipient). This is the
 * single place that fans operational events out to humans; it depends only on the published
 * {@code events} named interfaces of the producing modules.
 */
@Component
@RequiredArgsConstructor
public class EventNotificationListener {

    private final NotificationCreation notifications;

    @ApplicationModuleListener
    public void on(DeviceMarkedFaulty e) {
        notifications.notifyInApp(null, "device.faulty",
                "{\"serialNo\":\"%s\",\"reason\":\"%s\"}".formatted(e.serialNo(), safe(e.reason())),
                "device", e.deviceId());
    }

    @ApplicationModuleListener
    public void on(DeviceOfflineDetected e) {
        notifications.notifyInApp(null, "device.offline",
                "{\"serialNo\":\"%s\"}".formatted(e.serialNo()), "device", e.deviceId());
    }

    @ApplicationModuleListener
    public void on(ApprovalGranted e) {
        notifications.notifyInApp(null, "workflow.approved",
                "{\"type\":\"%s\",\"subject\":\"%s\"}".formatted(e.workflowType(), e.subjectType()),
                e.subjectType(), e.subjectId());
    }

    @ApplicationModuleListener
    public void on(DeploymentCompleted e) {
        notifications.notifyInApp(null, "deployment.completed",
                "{\"deploymentId\":\"%s\"}".formatted(e.deploymentId()), "deployment", e.deploymentId());
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace("\"", "'");
    }
}
