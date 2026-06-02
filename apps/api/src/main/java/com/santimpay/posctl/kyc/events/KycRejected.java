package com.santimpay.posctl.kyc.events;

import com.santimpay.posctl.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record KycRejected(
        UUID kycId,
        UUID merchantId,
        String reason,
        Instant occurredAt) implements DomainEvent {

    @Override public UUID aggregateId() { return kycId; }
    @Override public String aggregateType() { return "kyc"; }
}
