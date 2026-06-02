package com.santimpay.posctl.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.santimpay.posctl.ai.domain.MerchantFeatures;
import com.santimpay.posctl.ai.domain.MerchantScorer;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Pure scorer tests — no Spring/DB. Asserts the explainable models behave at the extremes. */
class MerchantScorerTest {

    private MerchantFeatures healthy() {
        return new MerchantFeatures(UUID.randomUUID(), true, true, 5, 8, 0,
                500, new BigDecimal("250000"), 400, 0, 1);
    }

    private MerchantFeatures struggling() {
        return new MerchantFeatures(UUID.randomUUID(), false, false, 1, 1, 2,
                2, new BigDecimal("500"), 50, 4, 60);
    }

    @Test
    void healthyMerchantScoresHighHealth_lowRisk() {
        var h = MerchantScorer.health(healthy());
        var r = MerchantScorer.risk(healthy());
        assertThat(h.value()).isGreaterThan(0.6);
        assertThat(h.band()).isEqualTo("high");
        assertThat(r.value()).isLessThan(0.34);
        assertThat(h.factors()).containsKey("txn_volume_30d"); // explanation present
    }

    @Test
    void strugglingMerchantScoresLowHealth_highRisk() {
        var h = MerchantScorer.health(struggling());
        var r = MerchantScorer.risk(struggling());
        assertThat(h.value()).isLessThan(0.34);
        assertThat(r.value()).isGreaterThan(0.6);
        assertThat(r.factors()).containsEntry("kyc_incomplete", 0.4); // dominant risk factor explained
    }

    @Test
    void scoresAlwaysBounded() {
        for (var f : new MerchantFeatures[]{healthy(), struggling()}) {
            assertThat(MerchantScorer.health(f).value()).isBetween(0.0, 1.0);
            assertThat(MerchantScorer.risk(f).value()).isBetween(0.0, 1.0);
        }
    }

    @Test
    void txnGrowthHandlesZeroBaseline() {
        var f = new MerchantFeatures(UUID.randomUUID(), true, true, 1, 1, 0,
                10, BigDecimal.TEN, 0, 0, 1);
        assertThat(f.txnGrowth()).isEqualTo(1.0); // new activity from zero baseline
    }
}
