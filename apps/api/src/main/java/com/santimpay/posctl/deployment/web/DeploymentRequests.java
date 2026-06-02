package com.santimpay.posctl.deployment.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public final class DeploymentRequests {

    private DeploymentRequests() {}

    public record PlanDeploymentRequest(
            @Size(max = 40) String deploymentNo,
            @NotNull LocalDate scheduledDate,
            @NotNull UUID merchantId,
            @NotNull UUID branchId,
            UUID assignedAgent) {}

    public record CompleteDeploymentRequest(
            @NotNull UUID deviceId,
            @Size(max = 160) String receivedBy,
            Double latitude,
            Double longitude,
            @Size(max = 4000) String conversationNotes,
            @Size(max = 64) String trelloCardId) {}
}
