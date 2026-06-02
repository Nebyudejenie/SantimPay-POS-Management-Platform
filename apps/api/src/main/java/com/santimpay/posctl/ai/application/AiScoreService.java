package com.santimpay.posctl.ai.application;

import com.santimpay.posctl.ai.domain.Score;
import com.santimpay.posctl.ai.events.MerchantScoreComputed;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read access to AI scores for the app (merchant overview, device fleet) plus the write hook the
 * offline scorer calls. Persisting a MERCHANT score also publishes {@link MerchantScoreComputed} so
 * downstream modules (followup) can react to risk/health changes — the AI module stays decoupled,
 * emitting facts rather than calling anyone.
 */
@Service
@RequiredArgsConstructor
public class AiScoreService {

    private final ScoreRepository repository;
    private final ApplicationEventPublisher events;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_report:read')")
    public List<Score> latestForSubject(String subjectType, UUID subjectId) {
        return repository.findLatestForSubject(subjectType, subjectId);
    }

    /** Called by the offline batch scorer (system context) to persist a computed score. */
    @Transactional
    public Score record(Score score) {
        Score saved = repository.save(score);
        if ("merchant".equals(saved.getSubjectType())) {
            events.publishEvent(new MerchantScoreComputed(
                    saved.getSubjectId(), saved.getScoreType(),
                    saved.getValue().doubleValue(), saved.getBand(), Instant.now()));
        }
        return saved;
    }
}
