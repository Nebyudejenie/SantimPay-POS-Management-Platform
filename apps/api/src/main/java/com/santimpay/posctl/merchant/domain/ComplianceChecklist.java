package com.santimpay.posctl.merchant.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/** Compliance audit trail — KYC, AML, identity, address, bank verification status. */
@Getter
@Entity
@Table(name = "compliance_checklist", schema = "merchant")
public class ComplianceChecklist extends AggregateRoot<ComplianceChecklist> {

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "check_type", nullable = false)
    private String checkType;  // kyc, aml, identity_verified, address_verified, bank_verified, license_verified, pep_check

    @Column(name = "check_status", nullable = false)
    private String status;  // PENDING, IN_PROGRESS, PASSED, FAILED, EXPIRED, PENDING_REVIEW

    @Column(name = "checked_by")
    private UUID checkedBy;

    @Column(name = "checked_at")
    private Instant checkedAt;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "findings")
    private String findings;

    @Column(name = "evidence_link")
    private String evidenceLink;

    protected ComplianceChecklist() {}

    public static ComplianceChecklist initiate(UUID merchantId, String checkType) {
        if (merchantId == null || checkType == null) {
            throw DomainException.invalidState("merchantId and checkType required");
        }
        ComplianceChecklist c = new ComplianceChecklist();
        c.assignIdentityIfAbsent();
        c.merchantId = merchantId;
        c.checkType = checkType;
        c.status = "PENDING";
        return c;
    }

    public void markPassed(UUID checkedBy, LocalDate expiryDate) {
        this.status = "PASSED";
        this.checkedBy = checkedBy;
        this.checkedAt = Instant.now();
        this.expiryDate = expiryDate;
    }

    public void markFailed(UUID checkedBy, String findings) {
        this.status = "FAILED";
        this.checkedBy = checkedBy;
        this.checkedAt = Instant.now();
        this.findings = findings;
    }

    public boolean isExpired() {
        return "PASSED".equals(status) && expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }
}
