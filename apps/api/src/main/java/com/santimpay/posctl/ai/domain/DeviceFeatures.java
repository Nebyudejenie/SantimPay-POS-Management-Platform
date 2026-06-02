package com.santimpay.posctl.ai.domain;

import java.util.UUID;

/**
 * Feature vector for POS-device failure prediction, snapshotted from the latest telemetry + device
 * record. Plain immutable record so {@link DeviceScorer} stays pure and unit-testable.
 *
 * @param recentFaultEvents fault/RMA events for this device in the trailing window
 * @param offlineRatio7d    fraction of telemetry samples reported "offline" over the last 7 days
 * @param avgBatteryLevel   mean battery % over recent samples (0–100; -1 if unknown)
 * @param minSignalStrength worst signal seen recently (lower = worse; -1 if unknown)
 * @param ageMonths         months since production/purchase date
 * @param daysSinceLastSeen telemetry silence (high = possibly already dead)
 */
public record DeviceFeatures(
        UUID deviceId,
        String serialNo,
        int recentFaultEvents,
        double offlineRatio7d,
        int avgBatteryLevel,
        int minSignalStrength,
        int ageMonths,
        int daysSinceLastSeen) {
}
