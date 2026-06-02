package com.santimpay.posctl.ai.infrastructure;

import com.santimpay.posctl.ai.application.EmbeddingPort;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Default embedding adapter: no-op. Returns an empty vector and {@code isEnabled()==false} so RAG
 * retrieval yields nothing and the assistant falls back to "I don't have grounded information" rather
 * than hallucinating. Replace with a real adapter (external API or local model) behind config.
 */
@Configuration
public class NoopEmbeddingAdapter {

    @Bean
    @ConditionalOnMissingBean(EmbeddingPort.class)
    public EmbeddingPort noopEmbedding() {
        return new EmbeddingPort() {
            @Override public List<Float> embed(String text) { return List.of(); }
            @Override public int dimension() { return 1024; }
            @Override public boolean isEnabled() { return false; }
        };
    }
}
