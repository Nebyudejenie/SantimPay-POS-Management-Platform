package com.santimpay.posctl.deployment.application;

import java.time.LocalDate;
import java.util.UUID;

public record PlanDeploymentCommand(
        String deploymentNo,
        LocalDate scheduledDate,
        UUID merchantId,
        UUID branchId,
        UUID assignedAgent) {}
