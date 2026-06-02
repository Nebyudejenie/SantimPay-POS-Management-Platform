package com.santimpay.posctl.deployment.application;

import java.util.UUID;

/** Field-completion input (from web or the Flutter sync engine). */
public record CompleteDeploymentCommand(
        UUID deploymentId,
        UUID deviceId,
        String receivedBy,
        Double latitude,
        Double longitude,
        String conversationNotes,
        String trelloCardId) {}
