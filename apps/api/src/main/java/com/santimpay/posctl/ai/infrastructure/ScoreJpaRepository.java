package com.santimpay.posctl.ai.infrastructure;

import com.santimpay.posctl.ai.domain.Score;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ScoreJpaRepository extends JpaRepository<Score, UUID> {

    /** Latest row per score_type for a subject (DISTINCT ON via native query). */
    @Query(value = """
           select distinct on (score_type) *
           from ai.scores
           where subject_type = :subjectType and subject_id = :subjectId
           order by score_type, computed_at desc
           """, nativeQuery = true)
    List<Score> findLatestForSubject(@Param("subjectType") String subjectType,
                                     @Param("subjectId") UUID subjectId);
}
