package com.santimpay.posctl.merchant.infrastructure;

import com.santimpay.posctl.merchant.domain.SettlementHistory;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementHistoryJpaRepository extends JpaRepository<SettlementHistory, UUID> {
    @Query("select s from SettlementHistory s where s.merchantId = :mid and s.audit.deletedAt is null order by s.periodTo desc")
    Page<SettlementHistory> findByMerchant(@Param("mid") UUID merchantId, Pageable pageable);
}
