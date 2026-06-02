package com.santimpay.posctl.ai.domain;

import com.santimpay.posctl.ai.domain.MerchantScorer.ScoreResult;
import java.util.LinkedHashMap;

/**
 * Phase-1 POS-device failure-probability scorer — transparent and rule-based, same philosophy as
 * {@link MerchantScorer}. Output 0..1 = probability the device needs intervention soon; the factor
 * map explains why (stored in {@code ai.scores.features}). A high score should drive a proactive
 * swap task before the merchant is impacted (docs/08 §15.3 "POS Failure Prediction").
 */
public final class DeviceScorer {

    private DeviceScorer() {}

    public static ScoreResult failureProbability(DeviceFeatures f) {
        var factors = new LinkedHashMap<String, Object>();
        double score = 0.0;

        // Prior faults are the strongest predictor of the next fault.
        double faults = Math.min(0.35, f.recentFaultEvents() * 0.15);
        factors.put("recent_fault_events", round(faults));
        score += faults;

        // Frequent offline periods => connectivity/hardware degradation.
        double offline = clamp(f.offlineRatio7d(), 0.0, 1.0) * 0.25;
        factors.put("offline_ratio_7d", round(offline));
        score += offline;

        // Chronic low battery.
        double battery = (f.avgBatteryLevel() >= 0 && f.avgBatteryLevel() < 20) ? 0.15 : 0.0;
        factors.put("low_battery", round(battery));
        score += battery;

        // Weak signal.
        double signal = (f.minSignalStrength() >= 0 && f.minSignalStrength() < 10) ? 0.1 : 0.0;
        factors.put("weak_signal", round(signal));
        score += signal;

        // Hardware age (gentle ramp past ~24 months).
        double age = f.ageMonths() > 24 ? Math.min(0.15, (f.ageMonths() - 24) * 0.01) : 0.0;
        factors.put("age", round(age));
        score += age;

        // Telemetry silence — possibly already failed.
        double silent = f.daysSinceLastSeen() > 3 ? 0.2 : 0.0;
        factors.put("telemetry_silence", round(silent));
        score += silent;

        double value = clamp(score, 0.0, 1.0);
        return new ScoreResult(value, band(value), factors);
    }

    private static String band(double v) {
        if (v < 0.34) return "low";
        if (v < 0.67) return "medium";
        return "high";
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double round(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
