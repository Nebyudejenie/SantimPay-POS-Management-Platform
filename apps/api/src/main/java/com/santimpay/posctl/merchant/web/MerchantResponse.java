package com.santimpay.posctl.merchant.web;

import java.time.Instant;
import java.util.UUID;

/** Web response representation of a merchant. */
public record MerchantResponse(
        UUID id,
        String merchantNo,
        String legalName,
        String tradeName,
        String taxId,
        String category,
        String status,
        String riskTier,
        Instant onboardedAt,
        Instant activatedAt,
        Instant createdAt,
        Instant updatedAt,
        int version) {}
