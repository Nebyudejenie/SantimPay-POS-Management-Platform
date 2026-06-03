package com.santimpay.posctl.kyc.application;

import com.santimpay.posctl.merchant.events.MerchantOnboarded;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Cross-module integration: when a merchant is onboarded, KYC opens an onboarding KYC request.
 *
 * <p>{@code @ApplicationModuleListener} = transactional + async event handling that Spring Modulith
 * tracks in the event publication registry (so a failed handler is retried, not lost). This is the
 * ONLY legitimate way kyc depends on merchant — it imports merchant's published event type, nothing
 * else.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantOnboardedListener {

    private final KycService kycService;

    @ApplicationModuleListener
    public void on(MerchantOnboarded event) {
        kycService.openOnboarding(event.merchantId());
        log.info("KYC: opened onboarding KYC request for merchant {} ({})",
                event.merchantNo(), event.merchantId());
    }
}
