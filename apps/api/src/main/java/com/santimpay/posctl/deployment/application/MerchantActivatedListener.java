package com.santimpay.posctl.deployment.application;

import com.santimpay.posctl.merchant.events.MerchantActivated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Deployment reacts to merchant activation: an activated merchant is now eligible for device
 * deployment. (Future: auto-create planned deployments for the merchant's branches / prioritize the
 * field queue.) Only dependency on merchant is its published event type — the correct seam.
 */
@Slf4j
@Component
public class MerchantActivatedListener {

    @ApplicationModuleListener
    public void on(MerchantActivated event) {
        log.info("Deployment: merchant {} activated — now eligible for POS deployment",
                event.merchantNo());
    }
}
