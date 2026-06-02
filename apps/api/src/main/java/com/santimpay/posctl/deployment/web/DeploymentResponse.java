package com.santimpay.posctl.deployment.web;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DeploymentResponse(
        UUID id,
        String deploymentNo,
        LocalDate scheduledDate,
        UUID merchantId,
        UUID branchId,
        UUID deviceId,
        UUID assignedAgent,
        String status,
        String receivedBy,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt,
        int version) {}
