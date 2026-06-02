package com.santimpay.posctl.kyc.events;

import com.santimpay.posctl.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/** Published when a KYC request is approved. Informational for analytics/notifications; the actual
 *  merchant activation is gated by a separate approval workflow initiated on approval. */
public record KycApproved(
        UUID kycId,
        UUID merchantId,
        Instant occurredAt) implements DomainEvent {

    @Override public UUID aggregateId() { return kycId; }
    @Override public String aggregateType() { return "kyc"; }
}
