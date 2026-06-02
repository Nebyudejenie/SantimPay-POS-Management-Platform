package com.santimpay.posctl.ai.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.santimpay.posctl.ai.domain.DeviceScorer;
import com.santimpay.posctl.ai.domain.MerchantFeatures;
import com.santimpay.posctl.ai.domain.MerchantScorer;
import com.santimpay.posctl.ai.domain.Score;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Offline batch scorer (Phase-1). Runs ONLY on the worker ({@code posctl.worker.enabled=true}) — the
 * AI module is decoupled from the request path (ADR-010). Nightly it reads each merchant's features,
 * computes explainable health + risk scores, and persists them; the app reads them via
 * {@link AiScoreService}. Idempotent: re-running just writes fresh score rows (latest wins).
 *
 * <p>The model version is recorded on every score so a model change is traceable and old scores stay
 * interpretable.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "posctl.worker.enabled", havingValue = "true")
public class ScoringJob {

    static final String MODEL_VERSION = "rule-v1";

    private final FeatureReader features;
    private final AiScoreService scores;
    private final ObjectMapper objectMapper;

    /** 02:30 daily (after the 02:00 DB base backup). Cron is overridable per-env. */
    @Scheduled(cron = "${posctl.ai.scoring-cron:0 30 2 * * *}")
    public void runNightlyScoring() {
        var ids = features.merchantIdsToScore();
        log.info("AI scoring: computing health+risk for {} merchants ({})", ids.size(), MODEL_VERSION);
        int n = 0;
        for (var id : ids) {
            try {
                MerchantFeatures f = features.featuresFor(id);
                persist("merchant", id, "health", MerchantScorer.health(f));
                persist("merchant", id, "risk", MerchantScorer.risk(f));
                n++;
            } catch (Exception e) {
                log.warn("AI scoring failed for merchant {}: {}", id, e.getMessage());
            }
        }
        log.info("AI scoring: completed {} merchants", n);

        // POS-device failure prediction over deployed/faulty devices (docs/08 §15.3).
        var deviceIds = features.deviceIdsToScore();
        log.info("AI scoring: computing failure_prob for {} devices", deviceIds.size());
        int d = 0;
        for (var id : deviceIds) {
            try {
                var df = features.deviceFeaturesFor(id);
                persist("device", id, "failure_prob", DeviceScorer.failureProbability(df));
                d++;
            } catch (Exception e) {
                log.warn("AI scoring failed for device {}: {}", id, e.getMessage());
            }
        }
        log.info("AI scoring: completed {} devices", d);
    }

    private void persist(String subjectType, java.util.UUID subjectId, String scoreType,
                         MerchantScorer.ScoreResult r) {
        scores.record(Score.of(subjectType, subjectId, scoreType,
                BigDecimal.valueOf(r.value()), r.band(), MODEL_VERSION, toJson(r.factors())));
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
