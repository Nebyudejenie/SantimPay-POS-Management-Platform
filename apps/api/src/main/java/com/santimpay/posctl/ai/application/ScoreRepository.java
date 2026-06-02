package com.santimpay.posctl.ai.application;

import com.santimpay.posctl.ai.domain.Score;
import java.util.List;
import java.util.UUID;

public interface ScoreRepository {

    Score save(Score score);

    /** Most recent score of each type for a subject (the app shows the latest). */
    List<Score> findLatestForSubject(String subjectType, UUID subjectId);
}
