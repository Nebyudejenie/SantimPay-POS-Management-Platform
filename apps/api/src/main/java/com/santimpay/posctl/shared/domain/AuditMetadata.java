package com.santimpay.posctl.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Standard audit columns embedded into every aggregate: who/when created & updated, optimistic
 * locking version, and soft-delete marker. Populated automatically by JPA auditing
 * ({@code @EnableJpaAuditing}) wired to {@link com.santimpay.posctl.shared.audit.AuditorAwareImpl}.
 */
@Getter
@Embeddable
@EntityListeners(AuditingEntityListener.class)
public class AuditMetadata {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted() {
        this.deletedAt = Instant.now();
    }
}
