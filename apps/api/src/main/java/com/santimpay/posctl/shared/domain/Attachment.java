package com.santimpay.posctl.shared.domain;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;

/** File attachment — documents, photos, signatures stored in S3/MinIO. */
@Getter
@Entity
@Table(name = "attachments", schema = "shared")
public class Attachment extends AggregateRoot<Attachment> {

    @Column(name = "entity_type", nullable = false)
    private String entityType;  // merchant, kyc, deployment, device, branch

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "document_type", nullable = false)
    private String documentType;  // license, id_photo, business_reg, deployment_photo, signature

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size")
    private Integer fileSize;

    @Column(name = "file_mime_type")
    private String fileMimeType;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "description")
    private String description;

    @Column(name = "uploaded_by")
    private UUID uploadedBy;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "verified_at")
    private java.time.Instant verifiedAt;

    protected Attachment() {}

    public static Attachment upload(String entityType, UUID entityId, String documentType,
                                    String fileName, Integer fileSize, String fileMimeType, String filePath) {
        if (entityType == null || entityId == null || fileName == null || filePath == null) {
            throw DomainException.invalidState("entityType, entityId, fileName, filePath required");
        }
        Attachment a = new Attachment();
        a.assignIdentityIfAbsent();
        a.entityType = entityType;
        a.entityId = entityId;
        a.documentType = documentType;
        a.fileName = fileName;
        a.fileSize = fileSize;
        a.fileMimeType = fileMimeType;
        a.filePath = filePath;
        return a;
    }

    public void markVerified(UUID verifiedBy) {
        this.verifiedBy = verifiedBy;
        this.verifiedAt = java.time.Instant.now();
    }
}
