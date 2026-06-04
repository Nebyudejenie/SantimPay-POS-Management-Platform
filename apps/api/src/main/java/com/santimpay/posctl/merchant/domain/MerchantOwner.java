package com.santimpay.posctl.merchant.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;

/**
 * A merchant's beneficial owner — the KYC subject(s) behind the business. A merchant can have several;
 * one is primary. Linked to the merchant by id.
 */
@Getter
@Entity
@Table(name = "merchant_owners", schema = "merchant")
public class MerchantOwner extends AggregateRoot<MerchantOwner> {

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "national_id")
    private String nationalId;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "ownership_pct")
    private BigDecimal ownershipPct;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    protected MerchantOwner() {}

    public static MerchantOwner create(UUID merchantId, String fullName, String nationalId,
                                       String phone, String email, BigDecimal ownershipPct,
                                       boolean primary) {
        if (merchantId == null) throw DomainException.invalidState("merchantId is required");
        if (fullName == null || fullName.isBlank()) throw DomainException.invalidState("fullName is required");
        MerchantOwner o = new MerchantOwner();
        o.assignIdentityIfAbsent();
        o.merchantId = merchantId;
        o.fullName = fullName;
        o.nationalId = nationalId;
        o.phone = phone;
        o.email = email;
        o.ownershipPct = ownershipPct;
        o.primary = primary;
        return o;
    }
}
