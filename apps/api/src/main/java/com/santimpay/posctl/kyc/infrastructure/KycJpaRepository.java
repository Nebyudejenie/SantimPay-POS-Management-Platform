package com.santimpay.posctl.kyc.infrastructure;

import com.santimpay.posctl.kyc.domain.KycRequest;
import com.santimpay.posctl.kyc.domain.KycStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface KycJpaRepository extends JpaRepository<KycRequest, UUID> {

    @Query("""
           select k from KycRequest k
           where k.audit.deletedAt is null
             and (:status is null or k.status = :status)
             and (:merchantId is null or k.merchantId = :merchantId)
           """)
    Page<KycRequest> search(@Param("status") KycStatus status,
                            @Param("merchantId") UUID merchantId,
                            Pageable pageable);
}
