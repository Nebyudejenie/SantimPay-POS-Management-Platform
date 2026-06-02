package com.santimpay.posctl.followup.infrastructure;

import com.santimpay.posctl.followup.domain.FollowUp;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface FollowUpJpaRepository extends JpaRepository<FollowUp, UUID> {

    @Query("""
           select f from FollowUp f
           where f.audit.deletedAt is null
             and (:merchantId is null or f.merchantId = :merchantId)
             and (:agentId is null or f.agentId = :agentId)
           """)
    Page<FollowUp> search(@Param("merchantId") UUID merchantId,
                          @Param("agentId") UUID agentId,
                          Pageable pageable);
}
