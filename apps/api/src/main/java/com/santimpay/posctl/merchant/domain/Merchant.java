package com.santimpay.posctl.merchant.domain;

import com.santimpay.posctl.merchant.events.MerchantActivated;
import com.santimpay.posctl.merchant.events.MerchantOnboarded;
import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

/**
 * Merchant aggregate root — the business onboarded to accept payments. Owners, branches and
 * settlement accounts are separate aggregates referenced by {@code merchantId} (kept out of this
 * aggregate so a large merchant with hundreds of branches is never loaded whole; see docs/02 §4.3).
 *
 * <p>State transitions are the aggregate's responsibility and the only place {@link MerchantStatus}
 * changes — this is the invariant boundary. Each transition raises a domain event captured by the
 * outbox.
 */
@Getter
@Entity
@Table(name = "merchants", schema = "merchant")
public class Merchant extends AggregateRoot<Merchant> {

    @Column(name = "merchant_no", nullable = false, unique = true, updatable = false)
    private String merchantNo;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "trade_name")
    private String tradeName;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "category")
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MerchantStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_tier")
    private RiskTier riskTier;

    @Column(name = "onboarded_at")
    private Instant onboardedAt;

    @Column(name = "activated_at")
    private Instant activatedAt;

    protected Merchant() {
        // for JPA
    }

    /** Factory: create a new merchant in ONBOARDING and raise {@link MerchantOnboarded}. */
    public static Merchant onboard(String merchantNo, String legalName, String tradeName,
                                   String taxId, String category) {
        if (merchantNo == null || merchantNo.isBlank()) {
            throw DomainException.invalidState("merchantNo is required");
        }
        if (legalName == null || legalName.isBlank()) {
            throw DomainException.invalidState("legalName is required");
        }
        Merchant m = new Merchant();
        m.assignIdentityIfAbsent();
        m.merchantNo = merchantNo;
        m.legalName = legalName;
        m.tradeName = tradeName;
        m.taxId = taxId;
        m.category = category;
        m.status = MerchantStatus.ONBOARDING;
        m.onboardedAt = Instant.now();
        m.riskTier = RiskTier.MEDIUM;
        m.raise(new MerchantOnboarded(m.getId(), merchantNo, legalName, Instant.now()));
        return m;
    }

    /** Transition to ACTIVE. Allowed only from ONBOARDING or PENDING_KYC (post-KYC + approval). */
    public void activate() {
        if (status != MerchantStatus.ONBOARDING && status != MerchantStatus.PENDING_KYC) {
            throw DomainException.conflict(
                    "Merchant cannot be activated from status " + status);
        }
        this.status = MerchantStatus.ACTIVE;
        this.activatedAt = Instant.now();
        raise(new MerchantActivated(getId(), merchantNo, Instant.now()));
    }

    public void markPendingKyc() {
        if (status != MerchantStatus.ONBOARDING) {
            throw DomainException.conflict("Only an onboarding merchant can move to PENDING_KYC");
        }
        this.status = MerchantStatus.PENDING_KYC;
    }

    public void suspend(String reason) {
        if (status != MerchantStatus.ACTIVE) {
            throw DomainException.conflict("Only an ACTIVE merchant can be suspended");
        }
        this.status = MerchantStatus.SUSPENDED;
    }

    public void assignRiskTier(RiskTier tier) {
        this.riskTier = tier;
    }
}
