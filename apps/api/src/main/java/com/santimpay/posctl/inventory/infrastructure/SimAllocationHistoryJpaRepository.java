package com.santimpay.posctl.inventory.infrastructure;

import com.santimpay.posctl.inventory.domain.SimAllocationHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimAllocationHistoryJpaRepository extends JpaRepository<SimAllocationHistory, UUID> {
    @Query("select a from SimAllocationHistory a where a.deviceId = :did and a.audit.deletedAt is null order by a.allocatedAt desc")
    List<SimAllocationHistory> findByDevice(@Param("did") UUID deviceId);

    @Query("select a from SimAllocationHistory a where a.deviceId = :did and a.deallocatedAt is null")
    SimAllocationHistory findCurrentByDevice(@Param("did") UUID deviceId);

    @Query("select a from SimAllocationHistory a where a.simId = :sid and a.deallocatedAt is null")
    SimAllocationHistory findCurrentBySim(@Param("sid") UUID simId);
}
