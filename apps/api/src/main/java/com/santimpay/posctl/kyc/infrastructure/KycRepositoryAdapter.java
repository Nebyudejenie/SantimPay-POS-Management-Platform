package com.santimpay.posctl.kyc.infrastructure;

import com.santimpay.posctl.kyc.application.KycRepository;
import com.santimpay.posctl.kyc.domain.KycRequest;
import com.santimpay.posctl.kyc.domain.KycStatus;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class KycRepositoryAdapter implements KycRepository {

    private final KycJpaRepository jpa;

    @Override
    public KycRequest save(KycRequest request) {
        return jpa.save(request);
    }

    @Override
    public Optional<KycRequest> findById(UUID id) {
        return jpa.findById(id).filter(k -> !k.getAudit().isDeleted());
    }

    @Override
    public Page<KycRequest> search(KycStatus status, UUID merchantId, Pageable pageable) {
        return jpa.search(status, merchantId, pageable);
    }
}
