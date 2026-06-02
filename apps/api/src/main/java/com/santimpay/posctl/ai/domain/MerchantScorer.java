package com.santimpay.posctl.ai.domain;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Phase-1 scorers: <b>transparent, rule-based, explainable</b> (docs/08 §15.3 — "start with
 * interpretable models, not deep nets"). Each returns a 0..1 value, a band, and the contributing
 * factors (the "SHAP-lite" explanation stored in {@code Score.features}) so every score is auditable
 * and human-overridable. These are deliberately simple weighted models; swapping in a trained
 * gradient-boosted model later means replacing the body, not the contract.
 */
public final class MerchantScorer {

    private MerchantScorer() {}

    public record ScoreResult(double value, String band, Map<String, Object> factors) {}

    /** Merchant HEALTH — higher is healthier. Engagement, growth, uptime, follow-up burden. */
    public static ScoreResult health(MerchantFeatures f) {
        var factors = new LinkedHashMap<String, Object>();
        double score = 0.0;

        double activity = f.active() ? 0.25 : 0.0;
        factors.put("active", activity);
        score += activity;

        // Recent transaction volume (saturating): some volume is healthy.
        double volume = Math.min(0.25, f.txnCountLast30d() / 400.0 * 0.25);
        factors.put("txn_volume_30d", round(volume));
        score += volume;

        // Growth trend, clamped to [-0.2, +0.2].
        double growth = clamp(f.txnGrowth() * 0.2, -0.2, 0.2);
        factors.put("txn_growth", round(growth));
        score += growth;

        // Device health: faulty devices drag the score down.
        double uptime = (1.0 - f.faultyDeviceRatio()) * 0.2;
        factors.put("device_uptime", round(uptime));
        score += uptime;

        // Unresolved follow-ups are a friction signal.
        double friction = Math.min(0.1, f.unresolvedFollowUps() * 0.025);
        factors.put("followup_friction_penalty", round(-friction));
        score -= friction;

        // Staleness penalty.
        double stale = f.daysSinceLastActivity() > 14 ? 0.1 : 0.0;
        factors.put("staleness_penalty", round(-stale));
        score -= stale;

        double value = clamp(score, 0.0, 1.0);
        return new ScoreResult(value, band(value), factors);
    }

    /** Merchant RISK — higher is riskier. Inverse-ish of health, weighted toward compliance signals. */
    public static ScoreResult risk(MerchantFeatures f) {
        var factors = new LinkedHashMap<String, Object>();
        double score = 0.0;

        double kyc = f.kycApproved() ? 0.0 : 0.4;          // un-KYC'd is the dominant risk
        factors.put("kyc_incomplete", kyc);
        score += kyc;

        double volatility = Math.min(0.2, Math.abs(f.txnGrowth()) * 0.2); // wild swings = risk
        factors.put("txn_volatility", round(volatility));
        score += volatility;

        double faults = f.faultyDeviceRatio() * 0.2;
        factors.put("device_fault_ratio", round(faults));
        score += faults;

        double dormant = f.daysSinceLastActivity() > 30 ? 0.2 : 0.0;
        factors.put("dormant", round(dormant));
        score += dormant;

        double value = clamp(score, 0.0, 1.0);
        return new ScoreResult(value, band(value), factors);
    }

    /** Bands match the DB CHECK semantics used elsewhere (low/medium/high). */
    private static String band(double v) {
        if (v < 0.34) return "low";
        if (v < 0.67) return "medium";
        return "high";
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double round(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
