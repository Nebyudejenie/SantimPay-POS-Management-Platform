package com.santimpay.posctl.shared.infrastructure;

import com.santimpay.posctl.shared.domain.Attachment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttachmentJpaRepository extends JpaRepository<Attachment, UUID> {
    @Query("select a from Attachment a where a.entityType = :type and a.entityId = :id and a.audit.deletedAt is null order by a.createdAt desc")
    List<Attachment> findByEntity(@Param("type") String entityType, @Param("id") UUID entityId);

    @Query("select a from Attachment a where a.entityType = :type and a.entityId = :id and a.documentType = :docType and a.audit.deletedAt is null")
    List<Attachment> findByEntityAndType(@Param("type") String entityType, @Param("id") UUID entityId, @Param("docType") String documentType);
}
