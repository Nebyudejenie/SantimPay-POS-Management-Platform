package com.santimpay.posctl.ai.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The feature vector for a merchant at scoring time — a snapshot of the signals the scorers consume.
 * Assembled by the feature reader from operational data (merchant status, branch/device footprint,
 * transaction trend, follow-up history). Kept as a plain immutable record so the scorers are pure and
 * unit-testable with no DB.
 */
public record MerchantFeatures(
        UUID merchantId,
        boolean active,
        boolean kycApproved,
        int branchCount,
        int activeDeviceCount,
        int faultyDeviceCount,
        long txnCountLast30d,
        BigDecimal txnAmountLast30d,
        long txnCountPrev30d,
        int unresolvedFollowUps,
        int daysSinceLastActivity) {

    /** Month-over-month transaction-count growth ratio (−1.0 .. +∞); 0 when no prior baseline. */
    public double txnGrowth() {
        if (txnCountPrev30d == 0) return txnCountLast30d > 0 ? 1.0 : 0.0;
        return (double) (txnCountLast30d - txnCountPrev30d) / txnCountPrev30d;
    }

    public double faultyDeviceRatio() {
        int total = activeDeviceCount + faultyDeviceCount;
        return total == 0 ? 0.0 : (double) faultyDeviceCount / total;
    }
}
