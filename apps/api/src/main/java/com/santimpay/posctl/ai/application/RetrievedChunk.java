package com.santimpay.posctl.ai.application;

import java.util.UUID;

/** One retrieved context chunk + its provenance (used to build citations). */
public record RetrievedChunk(
        UUID id,
        String sourceType,   // 'doc' | 'followup_note' | 'policy'
        UUID sourceId,
        String chunk,
        double distance) {   // cosine distance; lower = more relevant

    /** Similarity in [0,1] derived from cosine distance, for a confidence gate. */
    public double similarity() {
        return 1.0 - distance;
    }
}
