package com.santimpay.posctl.merchant.application;

import com.santimpay.posctl.merchant.domain.Merchant;
import com.santimpay.posctl.merchant.domain.MerchantStatus;
import com.santimpay.posctl.shared.domain.DomainException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service / use-case entry point for the merchant context. Each method is one use case,
 * transactional, and authorized with a fine-grained permission. Domain events raised by the
 * aggregate are flushed to the outbox within the same transaction on save.
 */
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository repository;

    @Transactional
    @PreAuthorize("hasAuthority('PERM_merchant:create')")
    public Merchant onboard(OnboardMerchantCommand cmd) {
        if (repository.existsByMerchantNo(cmd.merchantNo())) {
            throw DomainException.conflict("merchantNo already exists: " + cmd.merchantNo());
        }
        Merchant merchant = Merchant.onboard(
                cmd.merchantNo(), cmd.legalName(), cmd.tradeName(), cmd.taxId(), cmd.category());
        return repository.save(merchant);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_merchant:read')")
    public Merchant get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> DomainException.notFound("Merchant", id));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_merchant:read')")
    public Page<Merchant> search(String query, MerchantStatus status, Pageable pageable) {
        return repository.search(query, status, pageable);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_merchant:approve')")
    public Merchant activate(UUID id) {
        Merchant merchant = get(id);
        merchant.activate();
        return repository.save(merchant);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_merchant:update')")
    public Merchant suspend(UUID id, String reason) {
        Merchant merchant = get(id);
        merchant.suspend(reason);
        return repository.save(merchant);
    }
}
