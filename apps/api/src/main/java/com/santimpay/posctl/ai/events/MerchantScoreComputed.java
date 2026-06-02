package com.santimpay.posctl.ai.events;

import com.santimpay.posctl.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Published after the nightly scorer computes a merchant score. Lets other modules react to risk/
 * health changes — e.g. auto-generate a follow-up when health drops or risk spikes. Part of the ai
 * module's published contract.
 */
public record MerchantScoreComputed(
        UUID merchantId,
        String scoreType,   // "health" | "risk"
        double value,
        String band,        // low | medium | high
        Instant occurredAt) implements DomainEvent {

    @Override public UUID aggregateId() { return merchantId; }
    @Override public String aggregateType() { return "merchant_score"; }
}
