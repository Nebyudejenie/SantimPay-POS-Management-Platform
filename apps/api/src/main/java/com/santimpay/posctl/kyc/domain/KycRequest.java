package com.santimpay.posctl.kyc.domain;

import com.santimpay.posctl.kyc.events.KycApproved;
import com.santimpay.posctl.kyc.events.KycRejected;
import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/**
 * KYC request aggregate with its own state machine. References merchant/owner/reviewer by id.
 * Approval raises {@link KycApproved}; rejection raises {@link KycRejected}.
 */
@Getter
@Entity
@Table(name = "kyc_requests", schema = "kyc")
public class KycRequest extends AggregateRoot<KycRequest> {

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "request_type", nullable = false, updatable = false)
    private String requestType; // onboarding | update | periodic_review

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private KycStatus status;

    @Column(name = "reviewer_id")
    private UUID reviewerId;

    @Column(name = "decision_reason")
    private String decisionReason;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "external_ref")
    private String externalRef;

    protected KycRequest() {}

    /** Open an onboarding KYC request (typically triggered by MerchantOnboarded). Starts SUBMITTED. */
    public static KycRequest openOnboarding(UUID merchantId) {
        if (merchantId == null) {
            throw DomainException.invalidState("merchantId is required");
        }
        KycRequest k = new KycRequest();
        k.assignIdentityIfAbsent();
        k.merchantId = merchantId;
        k.requestType = "onboarding";
        k.status = KycStatus.SUBMITTED;
        k.submittedAt = Instant.now();
        return k;
    }

    public void assignReviewer(UUID reviewerId) {
        if (status != KycStatus.SUBMITTED && status != KycStatus.PENDING_DOCS) {
            throw DomainException.conflict("Can only assign a reviewer to a submitted/pending request");
        }
        this.reviewerId = reviewerId;
        this.status = KycStatus.UNDER_REVIEW;
    }

    public void requestMoreDocs(String note) {
        requireUnderReview();
        this.status = KycStatus.PENDING_DOCS;
        this.decisionReason = note;
    }

    public void approve(UUID reviewerId) {
        requireUnderReview();
        this.status = KycStatus.APPROVED;
        this.reviewerId = reviewerId;
        this.decidedAt = Instant.now();
        raise(new KycApproved(getId(), merchantId, Instant.now()));
    }

    public void reject(UUID reviewerId, String reason) {
        requireUnderReview();
        this.status = KycStatus.REJECTED;
        this.reviewerId = reviewerId;
        this.decisionReason = reason;
        this.decidedAt = Instant.now();
        raise(new KycRejected(getId(), merchantId, reason, Instant.now()));
    }

    private void requireUnderReview() {
        if (status != KycStatus.UNDER_REVIEW) {
            throw DomainException.conflict("KYC request must be UNDER_REVIEW (was " + status + ")");
        }
    }
}
