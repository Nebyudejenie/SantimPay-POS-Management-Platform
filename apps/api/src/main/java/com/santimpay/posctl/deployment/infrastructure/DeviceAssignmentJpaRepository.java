package com.santimpay.posctl.deployment.infrastructure;

import com.santimpay.posctl.deployment.domain.DeviceAssignment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface DeviceAssignmentJpaRepository extends JpaRepository<DeviceAssignment, UUID> {

    Optional<DeviceAssignment> findByDeviceIdAndCurrentTrue(UUID deviceId);
}
