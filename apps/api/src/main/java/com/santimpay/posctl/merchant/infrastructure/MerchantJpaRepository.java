package com.santimpay.posctl.merchant.infrastructure;

import com.santimpay.posctl.merchant.domain.Merchant;
import com.santimpay.posctl.merchant.domain.MerchantStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA interface. Internal to the infrastructure layer — the rest of the module talks to
 * {@link com.santimpay.posctl.merchant.application.MerchantRepository} (the port) via the adapter.
 * Soft-deleted rows ({@code deleted_at is not null}) are excluded from queries.
 */
interface MerchantJpaRepository extends JpaRepository<Merchant, UUID> {

    boolean existsByMerchantNo(String merchantNo);

    // Cast :q to string explicitly: when null, PostgreSQL otherwise infers the bind type as bytea
    // and fails with "function lower(bytea) does not exist". The cast pins it to varchar.
    @Query("""
           select m from Merchant m
           where m.audit.deletedAt is null
             and (:status is null or m.status = :status)
             and (cast(:q as string) is null
                  or lower(m.legalName) like lower(concat('%', cast(:q as string), '%'))
                  or lower(m.merchantNo) like lower(concat('%', cast(:q as string), '%')))
           """)
    Page<Merchant> search(@Param("q") String query,
                          @Param("status") MerchantStatus status,
                          Pageable pageable);
}
