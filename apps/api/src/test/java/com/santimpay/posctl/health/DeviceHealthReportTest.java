package com.santimpay.posctl.health;

import static org.assertj.core.api.Assertions.assertThat;

import com.santimpay.posctl.health.domain.DeviceHealthReport;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeviceHealthReportTest {

    @Test
    void offlineStatusIsDetected() {
        DeviceHealthReport offline = DeviceHealthReport.of(UUID.randomUUID(), "SN-1", "offline",
                10, "off", null, null, null, null, null, null, 0, "1.0", "13", Instant.now());
        DeviceHealthReport online = DeviceHealthReport.of(UUID.randomUUID(), "SN-2", "online",
                90, "on", null, null, null, null, null, null, 20, "1.0", "13", Instant.now());

        assertThat(offline.indicatesOffline()).isTrue();
        assertThat(online.indicatesOffline()).isFalse();
    }

    @Test
    void idIsTimeOrderedUuidV7() {
        DeviceHealthReport r = DeviceHealthReport.of(UUID.randomUUID(), "SN-3", "online",
                100, "on", null, null, null, null, null, null, null, null, null, null);
        // version nibble == 7
        assertThat((r.getId().version())).isEqualTo(7);
    }
}
