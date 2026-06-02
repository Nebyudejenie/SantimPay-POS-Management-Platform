package com.santimpay.posctl.kyc.application;

import com.santimpay.posctl.kyc.domain.KycRequest;
import com.santimpay.posctl.kyc.domain.KycStatus;
import com.santimpay.posctl.shared.domain.DomainException;
import com.santimpay.posctl.shared.security.CurrentUser;
import com.santimpay.posctl.workflow.application.WorkflowInitiation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * KYC use cases. On approval the request raises {@code KycApproved} AND a merchant-activation
 * approval workflow is initiated via {@link WorkflowInitiation} (workflow's published API) — so the
 * actual activation is checker-gated, not automatic. The kyc module depends on workflow's {@code api}
 * named interface and merchant's {@code events} named interface; it never imports their internals.
 */
@Service
@RequiredArgsConstructor
public class KycService {

    private final KycRepository repository;
    private final WorkflowInitiation workflows;

    /** Internal, system-context (no @PreAuthorize): invoked by the MerchantOnboarded event handler. */
    @Transactional
    public KycRequest openOnboarding(UUID merchantId) {
        return repository.save(KycRequest.openOnboarding(merchantId));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_kyc:read')")
    public KycRequest get(UUID id) {
        return repository.findById(id).orElseThrow(() -> DomainException.notFound("KycRequest", id));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_kyc:read')")
    public Page<KycRequest> search(KycStatus status, UUID merchantId, Pageable pageable) {
        return repository.search(status, merchantId, pageable);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_kyc:review')")
    public KycRequest assignToMe(UUID id) {
        KycRequest req = get(id);
        req.assignReviewer(CurrentUser.id().orElse(null));
        return repository.save(req);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_kyc:approve')")
    public KycRequest approve(UUID id) {
        UUID reviewer = CurrentUser.id().orElse(null);
        KycRequest req = get(id);
        req.approve(reviewer);
        repository.save(req);
        // Gate the actual activation behind a separate approval (maker≠checker).
        // Pass the type as a String — kyc must not import workflow.domain (ModularityTests).
        workflows.start("MERCHANT_ACTIVATION", "merchant", req.getMerchantId(), reviewer);
        return req;
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_kyc:approve')")
    public KycRequest reject(UUID id, String reason) {
        KycRequest req = get(id);
        req.reject(CurrentUser.id().orElse(null), reason);
        return repository.save(req);
    }
}
