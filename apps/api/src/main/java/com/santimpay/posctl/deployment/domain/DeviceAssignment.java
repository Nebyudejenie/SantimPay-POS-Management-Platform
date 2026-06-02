package com.santimpay.posctl.deployment.domain;

import com.santimpay.posctl.deployment.events.DeviceAssigned;
import com.santimpay.posctl.shared.domain.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/**
 * Temporal binding of a device to a branch/merchant. The "current" row has {@code valid_to = null}
 * and {@code is_current = true}. A Postgres exclusion constraint (see migration) makes it physically
 * impossible for a device to have two overlapping assignments — the DB enforces the invariant that
 * application code must respect by closing the prior assignment before opening a new one.
 */
@Getter
@Entity
@Table(name = "device_assignments", schema = "deployment")
public class DeviceAssignment extends AggregateRoot<DeviceAssignment> {

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "deployment_id")
    private UUID deploymentId;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "is_current", nullable = false)
    private boolean current;

    protected DeviceAssignment() {}

    public static DeviceAssignment open(UUID deviceId, UUID branchId, UUID merchantId, UUID deploymentId) {
        DeviceAssignment a = new DeviceAssignment();
        a.assignIdentityIfAbsent();
        a.deviceId = deviceId;
        a.branchId = branchId;
        a.merchantId = merchantId;
        a.deploymentId = deploymentId;
        a.validFrom = Instant.now();
        a.current = true;
        a.raise(new DeviceAssigned(a.getId(), deviceId, branchId, merchantId, deploymentId, Instant.now()));
        return a;
    }

    /** Close this assignment (device recovered / swapped). */
    public void close() {
        this.validTo = Instant.now();
        this.current = false;
    }
}
