package com.santimpay.posctl.merchant.application;

import com.santimpay.posctl.workflow.events.ApprovalGranted;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Closes the onboarding loop: when a MERCHANT_ACTIVATION workflow over a merchant subject is
 * approved, activate the merchant. Merchant depends only on workflow's published {@code events}
 * named interface — not its internals — keeping the dependency graph acyclic
 * (kyc→merchant, kyc→workflow, merchant→workflow).
 */
@Component
@RequiredArgsConstructor
public class ApprovalGrantedListener {

    private final MerchantActivationHandler activation;

    @ApplicationModuleListener
    public void on(ApprovalGranted event) {
        if ("merchant".equals(event.subjectType())
                && "MERCHANT_ACTIVATION".equals(event.workflowType())) {
            activation.activate(event.subjectId());
        }
    }
}
