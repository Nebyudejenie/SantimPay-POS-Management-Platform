package com.santimpay.posctl.merchant.infrastructure;

import com.santimpay.posctl.merchant.domain.MerchantDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MerchantDocumentJpaRepository extends JpaRepository<MerchantDocument, UUID> {
    @Query("select d from MerchantDocument d where d.merchantId = :mid and d.audit.deletedAt is null order by d.expiryDate asc")
    List<MerchantDocument> findByMerchant(@Param("mid") UUID merchantId);

    @Query("select d from MerchantDocument d where d.merchantId = :mid and d.documentType = :type and d.audit.deletedAt is null")
    List<MerchantDocument> findByMerchantAndType(@Param("mid") UUID merchantId, @Param("type") String documentType);
}
