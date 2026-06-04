package com.santimpay.posctl.merchant.infrastructure;

import com.santimpay.posctl.merchant.domain.Branch;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface BranchJpaRepository extends JpaRepository<Branch, UUID> {

    @Query("select b from Branch b where b.merchantId = :mid and b.audit.deletedAt is null order by b.branchNo")
    List<Branch> findByMerchant(@Param("mid") UUID merchantId);

    boolean existsByMerchantIdAndBranchNo(UUID merchantId, String branchNo);
}
