package com.santimpay.posctl.merchant.infrastructure;

import com.santimpay.posctl.merchant.domain.SettlementAccount;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementJpaRepository extends JpaRepository<SettlementAccount, UUID> {
    @Query("select s from SettlementAccount s where s.merchantId = :mid and s.audit.deletedAt is null order by s.primary desc")
    List<SettlementAccount> findByMerchant(@Param("mid") UUID merchantId);
}
