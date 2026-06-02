package com.santimpay.posctl.followup.application;

import com.santimpay.posctl.ai.application.LlmPort;
import com.santimpay.posctl.ai.events.MerchantScoreComputed;
import com.santimpay.posctl.followup.domain.FollowUp;
import com.santimpay.posctl.followup.domain.FollowUpChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Phase-3 automation seed (docs/08 §15.6): when a merchant's score signals trouble (health=low or
 * risk=high), draft a follow-up so an agent reaches out proactively. The draft is created as
 * {@code aiGenerated=true} and is NOT auto-sent — a human reviews it (build trust before autonomy).
 *
 * <p>Uses {@link LlmPort} for the suggested talking points when an LLM is configured, and falls back
 * to a deterministic template when it isn't ({@code isEnabled()==false}) — so this works with zero AI
 * spend out of the box. Reacts only to the ai module's published {@code MerchantScoreComputed} event.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoFollowUpGenerator {

    private final FollowUpRepository repository;
    private final LlmPort llm;

    @ApplicationModuleListener
    public void on(MerchantScoreComputed event) {
        boolean trouble = ("health".equals(event.scoreType()) && "low".equals(event.band()))
                || ("risk".equals(event.scoreType()) && "high".equals(event.band()));
        if (!trouble) return;

        String notes = draftNotes(event);
        FollowUp draft = FollowUp.aiDraft(event.merchantId(), FollowUpChannel.CALL, notes);
        repository.save(draft);
        log.info("Auto-drafted follow-up for merchant {} ({}={})",
                event.merchantId(), event.scoreType(), event.band());
    }

    private String draftNotes(MerchantScoreComputed e) {
        if (llm.isEnabled()) {
            String prompt = """
                    A merchant's %s score is %s (value %.2f). Draft 2-3 concise, friendly talking
                    points for a call-center agent to check in and offer help. No PII."""
                    .formatted(e.scoreType(), e.band(), e.value());
            String out = llm.complete("You are a SantimPay merchant-success assistant.", prompt);
            if (out != null && !out.isBlank()) return out;
        }
        // Deterministic fallback (no LLM).
        return "AI flag: merchant %s is %s. Proactively call to check on POS usage, resolve any "
                .formatted(e.scoreType(), e.band())
                + "issues, and confirm settlement details.";
    }
}
