package com.santimpay.posctl.inventory.domain;

import com.santimpay.posctl.inventory.events.DeviceMarkedFaulty;
import com.santimpay.posctl.inventory.events.DeviceReceived;
import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

/**
 * POS device aggregate. Owns its lifecycle state machine and the binding to an active SIM. Branch /
 * merchant assignment is NOT here — that's the deployment context's {@code DeviceAssignment}
 * aggregate (referenced by id), so a device's inventory state and its field placement evolve
 * independently.
 */
@Getter
@Entity
@Table(name = "pos_devices", schema = "inventory")
public class PosDevice extends AggregateRoot<PosDevice> {

    /** Legal transitions; the single source of truth for the state machine. */
    private static final Map<DeviceStatus, EnumSet<DeviceStatus>> TRANSITIONS = Map.of(
            DeviceStatus.IN_STOCK, EnumSet.of(DeviceStatus.ALLOCATED, DeviceStatus.LOST),
            DeviceStatus.ALLOCATED, EnumSet.of(DeviceStatus.DEPLOYED, DeviceStatus.IN_STOCK),
            DeviceStatus.DEPLOYED, EnumSet.of(DeviceStatus.FAULTY, DeviceStatus.IN_STOCK,
                    DeviceStatus.RETIRED, DeviceStatus.LOST),
            DeviceStatus.FAULTY, EnumSet.of(DeviceStatus.IN_REPAIR, DeviceStatus.RETIRED),
            DeviceStatus.IN_REPAIR, EnumSet.of(DeviceStatus.IN_STOCK, DeviceStatus.RETIRED));

    @Column(name = "serial_no", nullable = false, unique = true, updatable = false)
    private String serialNo;

    @Column(name = "terminal_id", unique = true)
    private String terminalId;

    @Column(name = "imei", unique = true)
    private String imei;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "vendor")
    private String vendor;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "kcv")
    private String kcv;

    @Column(name = "combined_kcv")
    private String combinedKcv;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviceStatus status;

    @Column(name = "active_sim_id")
    private UUID activeSimId;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "warranty_until")
    private LocalDate warrantyUntil;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    protected PosDevice() {}

    public DeviceStatus getStatus() { return status; }

    /** Factory: receive a brand-new device into stock and raise {@link DeviceReceived}. */
    public static PosDevice receiveIntoStock(String serialNo, String model, String vendor,
                                             String terminalId, String imei) {
        if (serialNo == null || serialNo.isBlank()) {
            throw DomainException.invalidState("serialNo is required");
        }
        if (model == null || model.isBlank()) {
            throw DomainException.invalidState("model is required");
        }
        PosDevice d = new PosDevice();
        d.assignIdentityIfAbsent();
        d.serialNo = serialNo;
        d.model = model;
        d.vendor = vendor;
        d.terminalId = terminalId;
        d.imei = imei;
        d.status = DeviceStatus.IN_STOCK;
        d.raise(new DeviceReceived(d.getId(), serialNo, model, Instant.now()));
        return d;
    }

    public void allocate() { transition(DeviceStatus.ALLOCATED); }

    public void markDeployed() {
        transition(DeviceStatus.DEPLOYED);
        this.lastActivityAt = Instant.now();
    }

    public void markFaulty(String reason) {
        transition(DeviceStatus.FAULTY);
        raise(new DeviceMarkedFaulty(getId(), serialNo, reason, Instant.now()));
    }

    public void sendToRepair() { transition(DeviceStatus.IN_REPAIR); }

    public void returnToStock() { transition(DeviceStatus.IN_STOCK); }

    public void retire() { transition(DeviceStatus.RETIRED); }

    public void bindSim(UUID simId) {
        this.activeSimId = simId;
    }

    private void transition(DeviceStatus target) {
        EnumSet<DeviceStatus> allowed = TRANSITIONS.getOrDefault(status, EnumSet.noneOf(DeviceStatus.class));
        if (!allowed.contains(target)) {
            throw DomainException.conflict(
                    "Illegal device transition %s -> %s".formatted(status, target));
        }
        this.status = target;
    }
}
