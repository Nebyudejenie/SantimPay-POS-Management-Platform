package com.santimpay.posctl.ai.infrastructure;

import com.santimpay.posctl.ai.application.RetrievedChunk;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * pgvector retrieval against {@code ai.embeddings}. Uses the HNSW cosine index (ix_embeddings_hnsw,
 * V1_0012) for approximate nearest-neighbour search. The query vector is passed as a pgvector
 * literal. Kept in infrastructure so the RAG service stays provider/DB-agnostic.
 */
@Component
@RequiredArgsConstructor
public class EmbeddingStore {

    private final JdbcTemplate jdbc;

    /** Top-k nearest chunks to the given embedding (cosine distance via the {@code <=>} operator). */
    public List<RetrievedChunk> nearest(List<Float> queryVector, int k) {
        if (queryVector == null || queryVector.isEmpty()) return List.of();
        String vec = toVectorLiteral(queryVector);
        return jdbc.query(
                """
                select id, source_type, source_id, chunk, (embedding <=> ?::vector) as distance
                from ai.embeddings
                order by embedding <=> ?::vector
                limit ?
                """,
                (rs, i) -> new RetrievedChunk(
                        rs.getObject("id", java.util.UUID.class),
                        rs.getString("source_type"),
                        rs.getObject("source_id", java.util.UUID.class),
                        rs.getString("chunk"),
                        rs.getDouble("distance")),
                vec, vec, k);
    }

    private String toVectorLiteral(List<Float> v) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(v.get(i));
        }
        return sb.append(']').toString();
    }
}
