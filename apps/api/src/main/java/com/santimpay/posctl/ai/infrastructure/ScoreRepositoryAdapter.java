package com.santimpay.posctl.ai.infrastructure;

import com.santimpay.posctl.ai.application.ScoreRepository;
import com.santimpay.posctl.ai.domain.Score;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ScoreRepositoryAdapter implements ScoreRepository {

    private final ScoreJpaRepository jpa;

    @Override
    public Score save(Score score) {
        return jpa.save(score);
    }

    @Override
    public List<Score> findLatestForSubject(String subjectType, UUID subjectId) {
        return jpa.findLatestForSubject(subjectType, subjectId);
    }
}
