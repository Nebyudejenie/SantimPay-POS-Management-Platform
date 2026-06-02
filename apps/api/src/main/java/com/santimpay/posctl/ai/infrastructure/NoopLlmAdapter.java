package com.santimpay.posctl.ai.infrastructure;

import com.santimpay.posctl.ai.application.LlmPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Default LLM adapter: a no-op used until a real provider is wired (Phase-2). Returning
 * {@code isEnabled()==false} lets callers fall back to deterministic templates, so the platform runs
 * with zero AI cost/dependency out of the box. A real adapter (external API or self-hosted vLLM,
 * behind a {@code posctl.ai.llm.*} config + its own {@code @ConditionalOnProperty} bean) replaces
 * this without touching any caller — that's the point of {@link LlmPort}.
 */
@Slf4j
@Configuration
public class NoopLlmAdapter {

    @Bean
    @ConditionalOnMissingBean(LlmPort.class)
    public LlmPort noopLlm() {
        return new LlmPort() {
            @Override
            public String complete(String systemPrompt, String userPrompt) {
                log.debug("LLM disabled — returning empty completion");
                return "";
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        };
    }
}
