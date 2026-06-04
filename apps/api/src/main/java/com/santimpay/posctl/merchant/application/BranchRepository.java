package com.santimpay.posctl.merchant.application;

import com.santimpay.posctl.merchant.domain.Branch;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepository {
    Branch save(Branch branch);
    Optional<Branch> findById(UUID id);
    List<Branch> findByMerchant(UUID merchantId);
    boolean existsByMerchantAndBranchNo(UUID merchantId, String branchNo);
}
