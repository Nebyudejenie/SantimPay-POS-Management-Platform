package com.santimpay.posctl.ai.application;

import java.util.List;

/**
 * Abstraction over the embedding model (same swap-the-provider philosophy as {@link LlmPort}).
 * Turns text into a vector for storage/retrieval against {@code ai.embeddings} (pgvector). Ships with
 * a no-op adapter so RAG degrades gracefully (empty retrieval) when no provider is configured.
 */
public interface EmbeddingPort {

    /** Embed a query/document chunk. Returns an empty list when no provider is configured. */
    List<Float> embed(String text);

    /** Dimension of produced vectors (must match the {@code vector(N)} column; 1024 in V1_0012). */
    int dimension();

    boolean isEnabled();
}
