package com.santimpay.posctl.merchant.domain;

/** Risk classification; set manually on onboarding and later refined by the AI risk score. */
public enum RiskTier {
    LOW,
    MEDIUM,
    HIGH
}
