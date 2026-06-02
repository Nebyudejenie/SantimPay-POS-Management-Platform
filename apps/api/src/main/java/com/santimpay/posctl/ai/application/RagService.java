package com.santimpay.posctl.ai.application;

import com.santimpay.posctl.ai.infrastructure.EmbeddingStore;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Retrieval-Augmented Generation assistant (docs/08 §15.4). Pipeline: embed the question → retrieve
 * nearest chunks from {@code ai.embeddings} → ground the LLM on ONLY those chunks → return the answer
 * with citations.
 *
 * <p>Guardrails (the whole point):
 *  - <b>Grounded-only:</b> if retrieval is empty or low-confidence, refuse rather than hallucinate.
 *  - <b>Graceful degradation:</b> with no embedding/LLM provider configured the no-op adapters make
 *    retrieval empty and we return the refusal — zero hallucination, zero cost.
 *  - <b>Permission-scoped:</b> guarded by {@code report:read}; callers must pass only data the user
 *    may see (PII redaction happens before chunks are indexed).
 */
@Service
@RequiredArgsConstructor
public class RagService {

    private static final int TOP_K = 6;
    private static final double MIN_SIMILARITY = 0.20; // below this, treat as "no grounded context"

    private final EmbeddingPort embedding;
    private final EmbeddingStore store;
    private final LlmPort llm;

    public record Answer(String text, List<RetrievedChunk> citations, boolean grounded) {}

    @PreAuthorize("hasAuthority('PERM_report:read')")
    public Answer ask(String question) {
        if (!embedding.isEnabled() || !llm.isEnabled()) {
            return new Answer(
                    "The AI assistant is not configured in this environment. "
                            + "Connect an embedding + LLM provider to enable grounded answers.",
                    List.of(), false);
        }

        List<RetrievedChunk> hits = store.nearest(embedding.embed(question), TOP_K).stream()
                .filter(c -> c.similarity() >= MIN_SIMILARITY)
                .toList();

        if (hits.isEmpty()) {
            return new Answer(
                    "I don't have grounded information to answer that. Try rephrasing, or check the "
                            + "relevant merchant/device record directly.",
                    List.of(), false);
        }

        String context = hits.stream()
                .map(c -> "- (%s) %s".formatted(c.sourceType(), c.chunk()))
                .collect(Collectors.joining("\n"));

        String system = """
                You are SantimPay's operations assistant. Answer ONLY from the provided context.
                If the context is insufficient, say so. Be concise. Do not invent facts or PII.""";
        String user = "Context:\n%s\n\nQuestion: %s".formatted(context, question);

        String text = llm.complete(system, user);
        return new Answer(text == null || text.isBlank()
                ? "No answer could be generated from the available context." : text, hits, true);
    }
}
