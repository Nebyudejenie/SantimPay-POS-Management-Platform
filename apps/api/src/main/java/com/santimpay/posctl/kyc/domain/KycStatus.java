package com.santimpay.posctl.kyc.domain;

/**
 * KYC request lifecycle (docs/02 §4.6). Transitions enforced by {@link KycRequest}:
 * <pre>
 *   DRAFT        -> SUBMITTED
 *   SUBMITTED    -> UNDER_REVIEW
 *   UNDER_REVIEW -> PENDING_DOCS | APPROVED | REJECTED
 *   PENDING_DOCS -> UNDER_REVIEW
 * </pre>
 */
public enum KycStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    PENDING_DOCS,
    APPROVED,
    REJECTED
}
