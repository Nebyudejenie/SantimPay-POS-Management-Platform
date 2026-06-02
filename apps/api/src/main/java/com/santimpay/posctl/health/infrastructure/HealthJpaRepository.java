package com.santimpay.posctl.health.infrastructure;

import com.santimpay.posctl.health.domain.DeviceHealthReport;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

interface HealthJpaRepository extends JpaRepository<DeviceHealthReport, UUID> {

    List<DeviceHealthReport> findByDeviceIdOrderByReportedAtDesc(UUID deviceId, Limit limit);
}
