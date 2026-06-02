package com.santimpay.posctl.inventory.web;

import java.time.Instant;
import java.util.UUID;

public record DeviceResponse(
        UUID id,
        String serialNo,
        String terminalId,
        String imei,
        String model,
        String vendor,
        String status,
        UUID activeSimId,
        Instant lastActivityAt,
        Instant createdAt,
        Instant updatedAt,
        int version) {}
