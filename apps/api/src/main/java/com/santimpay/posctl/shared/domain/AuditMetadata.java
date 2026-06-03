package com.santimpay.posctl.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * Standard audit columns embedded into every aggregate: who/when created & updated and the
 * soft-delete marker. Populated automatically by JPA auditing ({@code @EnableJpaAuditing}).
 *
 * <p>The {@code AuditingEntityListener} is registered on {@link AggregateRoot} (the
 * {@code @MappedSuperclass}), NOT here — Spring Data only fires {@code @CreatedDate}/{@code @CreatedBy}
 * when the listener is on the entity/superclass; on an {@code @Embeddable} it silently does nothing,
 * which leaves {@code created_at} null. The {@code @Version} likewise lives on {@link AggregateRoot}
 * (JPA forbids it in an {@code @Embeddable}).
 */
@Getter
@Embeddable
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

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted() {
        this.deletedAt = Instant.now();
    }
}
