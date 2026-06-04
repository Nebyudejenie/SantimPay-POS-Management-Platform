package com.santimpay.posctl.inventory.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/** SIM allocation history — which SIM was in which device, when. */
@Getter
@Entity
@Table(name = "sim_allocation_history", schema = "inventory")
public class SimAllocationHistory extends AggregateRoot<SimAllocationHistory> {

    @Column(name = "device_id", nullable = false, updatable = false)
    private UUID deviceId;

    @Column(name = "sim_id", nullable = false, updatable = false)
    private UUID simId;

    @Column(name = "allocated_at", nullable = false)
    private Instant allocatedAt;

    @Column(name = "deallocated_at")
    private Instant deallocatedAt;

    @Column(name = "reason")
    private String reason;  // initial_setup, sim_replacement, sim_upgrade, troubleshooting

    @Column(name = "allocated_by")
    private UUID allocatedBy;

    protected SimAllocationHistory() {}

    public static SimAllocationHistory allocate(UUID deviceId, UUID simId, String reason) {
        if (deviceId == null || simId == null) {
            throw DomainException.invalidState("deviceId and simId required");
        }
        SimAllocationHistory a = new SimAllocationHistory();
        a.assignIdentityIfAbsent();
        a.deviceId = deviceId;
        a.simId = simId;
        a.allocatedAt = Instant.now();
        a.reason = reason;
        return a;
    }

    public void deallocate() {
        this.deallocatedAt = Instant.now();
    }

    public boolean isActive() {
        return deallocatedAt == null;
    }
}
