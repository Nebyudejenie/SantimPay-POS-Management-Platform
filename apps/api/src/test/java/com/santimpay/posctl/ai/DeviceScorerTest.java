package com.santimpay.posctl.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.santimpay.posctl.ai.domain.DeviceFeatures;
import com.santimpay.posctl.ai.domain.DeviceScorer;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Pure failure-prediction tests — no Spring/DB. */
class DeviceScorerTest {

    private DeviceFeatures healthyDevice() {
        // no faults, always online, good battery/signal, young, seen today
        return new DeviceFeatures(UUID.randomUUID(), "SN-OK", 0, 0.0, 90, 25, 6, 0);
    }

    private DeviceFeatures dyingDevice() {
        // repeated faults, mostly offline, flat battery, weak signal, old, silent for days
        return new DeviceFeatures(UUID.randomUUID(), "SN-BAD", 3, 0.8, 5, 2, 36, 5);
    }

    @Test
    void healthyDeviceScoresLowFailureProbability() {
        var r = DeviceScorer.failureProbability(healthyDevice());
        assertThat(r.value()).isLessThan(0.34);
        assertThat(r.band()).isEqualTo("low");
    }

    @Test
    void dyingDeviceScoresHighFailureProbability_withExplanation() {
        var r = DeviceScorer.failureProbability(dyingDevice());
        assertThat(r.value()).isGreaterThan(0.67);
        assertThat(r.band()).isEqualTo("high");
        assertThat(r.factors()).containsKeys(
                "recent_fault_events", "offline_ratio_7d", "telemetry_silence");
    }

    @Test
    void scoreAlwaysBounded() {
        for (var f : new DeviceFeatures[]{healthyDevice(), dyingDevice()}) {
            assertThat(DeviceScorer.failureProbability(f).value()).isBetween(0.0, 1.0);
        }
    }
}
