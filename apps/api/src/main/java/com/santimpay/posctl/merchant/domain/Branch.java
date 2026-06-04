package com.santimpay.posctl.merchant.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;

/**
 * A merchant branch — a physical location where POS terminals operate. Created during data-entry
 * intake (DATA_ENCODER) and referenced by deployments. Its own aggregate, linked to the merchant by
 * id (a large merchant can have hundreds of branches; we never load them all into the Merchant).
 */
@Getter
@Entity
@Table(name = "branches", schema = "merchant")
public class Branch extends AggregateRoot<Branch> {

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "branch_no", nullable = false)
    private String branchNo;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "region")
    private String region;
    @Column(name = "city")
    private String city;
    @Column(name = "sub_city")
    private String subCity;
    @Column(name = "woreda")
    private String woreda;
    @Column(name = "address_line")
    private String addressLine;

    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "status", nullable = false)
    private String status;

    protected Branch() {}

    public static Branch create(UUID merchantId, String branchNo, String name, String region,
                                String city, String subCity, String woreda, String addressLine,
                                String contactPhone, Double latitude, Double longitude) {
        if (merchantId == null) throw DomainException.invalidState("merchantId is required");
        if (branchNo == null || branchNo.isBlank()) throw DomainException.invalidState("branchNo is required");
        if (name == null || name.isBlank()) throw DomainException.invalidState("name is required");
        Branch b = new Branch();
        b.assignIdentityIfAbsent();
        b.merchantId = merchantId;
        b.branchNo = branchNo;
        b.name = name;
        b.region = region;
        b.city = city;
        b.subCity = subCity;
        b.woreda = woreda;
        b.addressLine = addressLine;
        b.contactPhone = contactPhone;
        b.latitude = latitude;
        b.longitude = longitude;
        b.status = "active";
        return b;
    }

    public void update(String name, String region, String city, String contactPhone) {
        if (name != null && !name.isBlank()) this.name = name;
        if (region != null) this.region = region;
        if (city != null) this.city = city;
        if (contactPhone != null) this.contactPhone = contactPhone;
    }

    public void close() { this.status = "closed"; }
}
