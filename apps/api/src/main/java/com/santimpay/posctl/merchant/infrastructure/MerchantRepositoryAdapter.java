package com.santimpay.posctl.merchant.infrastructure;

import com.santimpay.posctl.merchant.application.MerchantRepository;
import com.santimpay.posctl.merchant.domain.Merchant;
import com.santimpay.posctl.merchant.domain.MerchantStatus;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/** Adapter implementing the {@link MerchantRepository} port over Spring Data JPA. */
@Component
@RequiredArgsConstructor
class MerchantRepositoryAdapter implements MerchantRepository {

    private final MerchantJpaRepository jpa;

    @Override
    public Merchant save(Merchant merchant) {
        return jpa.save(merchant);
    }

    @Override
    public Optional<Merchant> findById(UUID id) {
        return jpa.findById(id).filter(m -> !m.getAudit().isDeleted());
    }

    @Override
    public boolean existsByMerchantNo(String merchantNo) {
        return jpa.existsByMerchantNo(merchantNo);
    }

    @Override
    public Page<Merchant> search(String query, MerchantStatus status, Pageable pageable) {
        return jpa.search(query, status, pageable);
    }
}
