package com.santimpay.posctl.kyc.application;

import com.santimpay.posctl.merchant.events.MerchantOnboarded;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Cross-module integration: when a merchant is onboarded, KYC opens an onboarding KYC request.
 *
 * <p>{@code @ApplicationModuleListener} = transactional + async event handling that Spring Modulith
 * tracks in the event publication registry (so a failed handler is retried, not lost). This is the
 * ONLY legitimate way kyc depends on merchant — it imports merchant's published event type, nothing
 * else. The actual KycRequest aggregate + service are added in the kyc build step (see
 * IMPLEMENTATION.md §7 step 5); this seam is wired now so the contract is real and tested.
 */
@Slf4j
@Component
public class MerchantOnboardedListener {

    @ApplicationModuleListener
    public void on(MerchantOnboarded event) {
        // TODO(kyc build step): kycService.openOnboardingRequest(event.merchantId());
        log.info("KYC: opening onboarding KYC request for merchant {} ({})",
                event.merchantNo(), event.merchantId());
    }
}
