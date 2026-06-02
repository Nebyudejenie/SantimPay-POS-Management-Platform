package com.santimpay.posctl.merchant.events;

import com.santimpay.posctl.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Published when a merchant becomes ACTIVE (after KYC + approval). Consumed by deployment (device
 * deployment may now proceed), analytics and notifications.
 */
public record MerchantActivated(
        UUID merchantId,
        String merchantNo,
        Instant occurredAt) implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return merchantId;
    }

    @Override
    public String aggregateType() {
        return "merchant";
    }
}
