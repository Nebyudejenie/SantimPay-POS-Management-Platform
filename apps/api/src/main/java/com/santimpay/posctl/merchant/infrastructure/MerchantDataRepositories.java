package com.santimpay.posctl.merchant.infrastructure;

import com.santimpay.posctl.merchant.domain.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SettlementHistoryJpaRepository extends JpaRepository<SettlementHistory, UUID> {
    @Query("select s from SettlementHistory s where s.merchantId = :mid and s.audit.deletedAt is null order by s.periodTo desc")
    Page<SettlementHistory> findByMerchant(@Param("mid") UUID merchantId, Pageable pageable);
}

interface MerchantDocumentJpaRepository extends JpaRepository<MerchantDocument, UUID> {
    @Query("select d from MerchantDocument d where d.merchantId = :mid and d.audit.deletedAt is null order by d.expiryDate asc")
    List<MerchantDocument> findByMerchant(@Param("mid") UUID merchantId);

    @Query("select d from MerchantDocument d where d.merchantId = :mid and d.documentType = :type and d.audit.deletedAt is null")
    List<MerchantDocument> findByMerchantAndType(@Param("mid") UUID merchantId, @Param("type") String documentType);
}

interface ComplianceChecklistJpaRepository extends JpaRepository<ComplianceChecklist, UUID> {
    @Query("select c from ComplianceChecklist c where c.merchantId = :mid and c.audit.deletedAt is null order by c.checkedAt desc")
    List<ComplianceChecklist> findByMerchant(@Param("mid") UUID merchantId);

    @Query("select c from ComplianceChecklist c where c.merchantId = :mid and c.checkType = :type and c.audit.deletedAt is null")
    ComplianceChecklist findLatestCheck(@Param("mid") UUID merchantId, @Param("type") String checkType);
}

interface MerchantStakeholderJpaRepository extends JpaRepository<MerchantStakeholder, UUID> {
    @Query("select s from MerchantStakeholder s where s.merchantId = :mid and s.audit.deletedAt is null order by s.isPrimary desc, s.fullName")
    List<MerchantStakeholder> findByMerchant(@Param("mid") UUID merchantId);

    @Query("select s from MerchantStakeholder s where s.merchantId = :mid and s.role = :role and s.audit.deletedAt is null")
    List<MerchantStakeholder> findByMerchantAndRole(@Param("mid") UUID merchantId, @Param("role") String role);
}
