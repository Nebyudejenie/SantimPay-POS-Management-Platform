package com.santimpay.posctl.inventory.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/** Device maintenance record — repairs, upgrades, inspections. */
@Getter
@Entity
@Table(name = "device_maintenance", schema = "inventory")
public class DeviceMaintenance extends AggregateRoot<DeviceMaintenance> {

    @Column(name = "device_id", nullable = false, updatable = false)
    private UUID deviceId;

    @Column(name = "maintenance_type", nullable = false)
    private String maintenanceType;  // repair, replacement, upgrade, inspection, software_update

    @Column(name = "issue_description")
    private String issueDescription;

    @Column(name = "resolution")
    private String resolution;

    @Column(name = "parts_replaced")
    private String partsReplaced;

    @Column(name = "technician_id")
    private UUID technicianId;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "completion_date")
    private Instant completionDate;

    @Column(name = "cost")
    private BigDecimal cost;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "warranty_claim")
    private boolean warrantyClaim;

    protected DeviceMaintenance() {}

    public UUID getDeviceId() { return deviceId; }
    public String getMaintenanceType() { return maintenanceType; }
    public String getIssueDescription() { return issueDescription; }
    public String getResolution() { return resolution; }
    public String getPartsReplaced() { return partsReplaced; }
    public UUID getTechnicianId() { return technicianId; }
    public Instant getStartDate() { return startDate; }
    public Instant getCompletionDate() { return completionDate; }
    public BigDecimal getCost() { return cost; }
    public String getVendorName() { return vendorName; }
    public boolean isWarrantyClaim() { return warrantyClaim; }

    public static DeviceMaintenance initiate(UUID deviceId, String maintenanceType, String issueDescription) {
        if (deviceId == null || maintenanceType == null) {
            throw DomainException.invalidState("deviceId and maintenanceType required");
        }
        DeviceMaintenance m = new DeviceMaintenance();
        m.assignIdentityIfAbsent();
        m.deviceId = deviceId;
        m.maintenanceType = maintenanceType;
        m.issueDescription = issueDescription;
        m.startDate = Instant.now();
        return m;
    }

    public void complete(String resolution, UUID technicianId) {
        this.resolution = resolution;
        this.technicianId = technicianId;
        this.completionDate = Instant.now();
    }
}
