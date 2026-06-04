package com.santimpay.posctl.merchant.infrastructure;

import com.santimpay.posctl.merchant.domain.MerchantStakeholder;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MerchantStakeholderJpaRepository extends JpaRepository<MerchantStakeholder, UUID> {
    @Query("select s from MerchantStakeholder s where s.merchantId = :mid and s.audit.deletedAt is null order by s.isPrimary desc, s.fullName")
    List<MerchantStakeholder> findByMerchant(@Param("mid") UUID merchantId);

    @Query("select s from MerchantStakeholder s where s.merchantId = :mid and s.role = :role and s.audit.deletedAt is null")
    List<MerchantStakeholder> findByMerchantAndRole(@Param("mid") UUID merchantId, @Param("role") String role);
}
