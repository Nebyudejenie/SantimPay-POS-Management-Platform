package com.santimpay.posctl.merchant.events;

import com.santimpay.posctl.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Published when a merchant is first created. Consumed by kyc (open a KYC request), analytics, and
 * notifications. Part of the merchant module's public contract.
 */
public record MerchantOnboarded(
        UUID merchantId,
        String merchantNo,
        String legalName,
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
