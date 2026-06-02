package com.santimpay.posctl.inventory.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class DeviceRequests {

    private DeviceRequests() {}

    public record ReceiveDeviceRequest(
            @NotBlank @Size(max = 60) String serialNo,
            @NotBlank @Size(max = 60) String model,
            @Size(max = 60) String vendor,
            @Size(max = 40) String terminalId,
            @Size(max = 20) String imei) {}

    public record MarkFaultyRequest(
            @NotBlank @Size(max = 500) String reason) {}
}
