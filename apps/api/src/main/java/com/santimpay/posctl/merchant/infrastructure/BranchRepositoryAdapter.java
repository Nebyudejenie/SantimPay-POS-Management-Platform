package com.santimpay.posctl.merchant.infrastructure;

import com.santimpay.posctl.merchant.application.BranchRepository;
import com.santimpay.posctl.merchant.domain.Branch;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class BranchRepositoryAdapter implements BranchRepository {

    private final BranchJpaRepository jpa;

    @Override
    public Branch save(Branch branch) {
        return jpa.save(branch);
    }

    @Override
    public Optional<Branch> findById(UUID id) {
        return jpa.findById(id).filter(b -> !b.getAudit().isDeleted());
    }

    @Override
    public List<Branch> findByMerchant(UUID merchantId) {
        return jpa.findByMerchant(merchantId);
    }

    @Override
    public boolean existsByMerchantAndBranchNo(UUID merchantId, String branchNo) {
        return jpa.existsByMerchantIdAndBranchNo(merchantId, branchNo);
    }
}
