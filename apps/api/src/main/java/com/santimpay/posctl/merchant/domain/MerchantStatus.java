package com.santimpay.posctl.merchant.domain;

/** Lifecycle of a merchant. Transitions are guarded by {@link Merchant} behavior. */
public enum MerchantStatus {
    ONBOARDING,
    PENDING_KYC,
    ACTIVE,
    SUSPENDED,
    CLOSED
}
