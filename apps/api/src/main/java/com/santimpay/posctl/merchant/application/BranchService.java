package com.santimpay.posctl.merchant.application;

import com.santimpay.posctl.merchant.domain.Branch;
import com.santimpay.posctl.shared.domain.DomainException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Branch intake use cases — part of the merchant context. Branch CRUD is gated by merchant perms. */
@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository repository;

    @Transactional
    @PreAuthorize("hasAuthority('PERM_merchant:create')")
    public Branch add(UUID merchantId, String branchNo, String name, String region, String city,
                      String subCity, String woreda, String addressLine, String contactPhone,
                      Double latitude, Double longitude) {
        if (repository.existsByMerchantAndBranchNo(merchantId, branchNo)) {
            throw DomainException.conflict("branchNo already exists for this merchant: " + branchNo);
        }
        return repository.save(Branch.create(merchantId, branchNo, name, region, city, subCity,
                woreda, addressLine, contactPhone, latitude, longitude));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_merchant:read')")
    public List<Branch> list(UUID merchantId) {
        return repository.findByMerchant(merchantId);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_merchant:update')")
    public Branch update(UUID id, String name, String region, String city, String contactPhone) {
        Branch b = repository.findById(id).orElseThrow(() -> DomainException.notFound("Branch", id));
        b.update(name, region, city, contactPhone);
        return repository.save(b);
    }
}
