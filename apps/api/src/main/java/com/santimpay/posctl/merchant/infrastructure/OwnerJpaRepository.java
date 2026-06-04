package com.santimpay.posctl.merchant.infrastructure;

import com.santimpay.posctl.merchant.domain.MerchantOwner;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OwnerJpaRepository extends JpaRepository<MerchantOwner, UUID> {
    @Query("select o from MerchantOwner o where o.merchantId = :mid and o.audit.deletedAt is null order by o.primary desc, o.fullName")
    List<MerchantOwner> findByMerchant(@Param("mid") UUID merchantId);
}
