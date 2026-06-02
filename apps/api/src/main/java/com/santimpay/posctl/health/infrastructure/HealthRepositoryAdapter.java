package com.santimpay.posctl.health.infrastructure;

import com.santimpay.posctl.health.application.HealthRepository;
import com.santimpay.posctl.health.domain.DeviceHealthReport;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class HealthRepositoryAdapter implements HealthRepository {

    private final HealthJpaRepository jpa;

    @Override
    public DeviceHealthReport save(DeviceHealthReport report) {
        return jpa.save(report);
    }

    @Override
    public List<DeviceHealthReport> saveAll(List<DeviceHealthReport> reports) {
        return jpa.saveAll(reports);
    }

    @Override
    public Optional<DeviceHealthReport> findLatestByDevice(UUID deviceId) {
        return jpa.findByDeviceIdOrderByReportedAtDesc(deviceId, Limit.of(1)).stream().findFirst();
    }
}
