package com.santimpay.posctl.inventory.infrastructure;

import com.santimpay.posctl.inventory.domain.DeviceMaintenance;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceMaintenanceJpaRepository extends JpaRepository<DeviceMaintenance, UUID> {
    @Query("select m from DeviceMaintenance m where m.deviceId = :did and m.audit.deletedAt is null order by m.startDate desc")
    List<DeviceMaintenance> findByDevice(@Param("did") UUID deviceId);

    @Query("select m from DeviceMaintenance m where m.deviceId = :did and m.completionDate is null")
    List<DeviceMaintenance> findOpenByDevice(@Param("did") UUID deviceId);
}
