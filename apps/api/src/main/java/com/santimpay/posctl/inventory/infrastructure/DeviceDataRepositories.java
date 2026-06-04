package com.santimpay.posctl.inventory.infrastructure;

import com.santimpay.posctl.inventory.domain.DeviceMaintenance;
import com.santimpay.posctl.inventory.domain.SimAllocationHistory;
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

public interface SimAllocationHistoryJpaRepository extends JpaRepository<SimAllocationHistory, UUID> {
    @Query("select a from SimAllocationHistory a where a.deviceId = :did and a.audit.deletedAt is null order by a.allocatedAt desc")
    List<SimAllocationHistory> findByDevice(@Param("did") UUID deviceId);

    @Query("select a from SimAllocationHistory a where a.deviceId = :did and a.deallocatedAt is null")
    SimAllocationHistory findCurrentByDevice(@Param("did") UUID deviceId);

    @Query("select a from SimAllocationHistory a where a.simId = :sid and a.deallocatedAt is null")
    SimAllocationHistory findCurrentBySim(@Param("sid") UUID simId);
}
