package com.santimpay.posctl.ai.application;

import org.springframework.modulith.NamedInterface;

/**
 * The single abstraction over a Large Language Model (docs/08 §15.5). Phase-2 RAG + generation goes
 * through this port so the provider (external API now; self-hosted vLLM later) is a config/adapter
 * swap, never a rewrite. Exposed as a {@code @NamedInterface} so other modules could request
 * generated text via the ai module without binding to a vendor SDK.
 *
 * <p>Prompts passed here must already be permission-scoped + PII-redacted by the caller — the port is
 * dumb on purpose; guardrails live in the application services that use it.
 */
@NamedInterface("llm")
public interface LlmPort {

    /** Generate a short completion for a prompt. Implementations must be side-effect free. */
    String complete(String systemPrompt, String userPrompt);

    /** Whether a real provider is configured (false = no-op stub; callers fall back to templates). */
    boolean isEnabled();
}
