package com.santimpay.posctl.ai.domain;

import com.santimpay.posctl.shared.domain.UuidV7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A computed AI/ML score for a subject (merchant risk/health/sales, device failure probability).
 * Written by offline batch scoring jobs (Phase 1) and read by the app — the {@code ai} module is
 * never in the write path of the core domain (ADR-010). {@code features} holds the SHAP/explanation
 * blob so scores stay explainable and overridable.
 */
@Getter
@Entity
@Table(name = "scores", schema = "ai")
public class Score {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "subject_type", nullable = false)
    private String subjectType;       // 'merchant' | 'device'

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "score_type", nullable = false)
    private String scoreType;         // 'risk' | 'health' | 'sales' | 'failure_prob'

    @Column(name = "value", nullable = false)
    private BigDecimal value;

    @Column(name = "band")
    private String band;              // 'low' | 'medium' | 'high'

    @Column(name = "model_version", nullable = false)
    private String modelVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", columnDefinition = "jsonb")
    private String features;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    protected Score() {}

    public static Score of(String subjectType, UUID subjectId, String scoreType, BigDecimal value,
                           String band, String modelVersion, String featuresJson) {
        Score s = new Score();
        s.id = UuidV7.generate();
        s.subjectType = subjectType;
        s.subjectId = subjectId;
        s.scoreType = scoreType;
        s.value = value;
        s.band = band;
        s.modelVersion = modelVersion;
        s.features = featuresJson;
        s.computedAt = Instant.now();
        return s;
    }
}
