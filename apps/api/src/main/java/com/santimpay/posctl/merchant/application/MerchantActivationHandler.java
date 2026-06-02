package com.santimpay.posctl.merchant.application;

import com.santimpay.posctl.shared.domain.DomainException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.santimpay.posctl.merchant.domain.Merchant;

/**
 * Internal activation driven by an approved workflow (no {@code @PreAuthorize}: runs in system
 * context from an event handler). Idempotent — a replayed ApprovalGranted on an already-active
 * merchant is a no-op.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantActivationHandler {

    private final MerchantRepository repository;

    @Transactional
    public void activate(UUID merchantId) {
        Merchant merchant = repository.findById(merchantId)
                .orElseThrow(() -> DomainException.notFound("Merchant", merchantId));
        try {
            merchant.activate();
            repository.save(merchant);
            log.info("Merchant {} activated via approved workflow", merchantId);
        } catch (DomainException e) {
            // already active / not in an activatable state — safe to ignore on replay
            log.debug("Skipping activation for merchant {}: {}", merchantId, e.getMessage());
        }
    }
}
