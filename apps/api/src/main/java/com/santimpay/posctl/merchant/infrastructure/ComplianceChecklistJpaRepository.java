package com.santimpay.posctl.merchant.infrastructure;

import com.santimpay.posctl.merchant.domain.ComplianceChecklist;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComplianceChecklistJpaRepository extends JpaRepository<ComplianceChecklist, UUID> {
    @Query("select c from ComplianceChecklist c where c.merchantId = :mid and c.audit.deletedAt is null order by c.checkedAt desc")
    List<ComplianceChecklist> findByMerchant(@Param("mid") UUID merchantId);

    @Query("select c from ComplianceChecklist c where c.merchantId = :mid and c.checkType = :type and c.audit.deletedAt is null")
    ComplianceChecklist findLatestCheck(@Param("mid") UUID merchantId, @Param("type") String checkType);
}
