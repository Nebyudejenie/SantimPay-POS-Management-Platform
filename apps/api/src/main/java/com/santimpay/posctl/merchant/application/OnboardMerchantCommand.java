package com.santimpay.posctl.merchant.application;

/** Use-case input for onboarding a merchant. Validated at the web edge before reaching here. */
public record OnboardMerchantCommand(
        String merchantNo,
        String legalName,
        String tradeName,
        String taxId,
        String category) {}
