package com.santimpay.posctl.inventory.application;

/** Use-case input for receiving a device into stock (single or via bulk import). */
public record ReceiveDeviceCommand(
        String serialNo,
        String model,
        String vendor,
        String terminalId,
        String imei) {}
