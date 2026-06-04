package com.santimpay.posctl.shared.domain;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;

/** General remark/annotation on any entity — issues, notes, followups, escalations. */
@Getter
@Entity
@Table(name = "remarks", schema = "shared")
public class Remark {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "entity_type", nullable = false)
    private String entityType;  // merchant, device, deployment, kyc, branch

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "remark_type")
    private String remarkType;  // issue, note, followup, risk, escalation

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "priority")
    private String priority;  // LOW, NORMAL, HIGH, URGENT

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at")
    private java.time.Instant createdAt;

    @Column(name = "updated_at")
    private java.time.Instant updatedAt;

    @Column(name = "deleted_at")
    private java.time.Instant deletedAt;

    protected Remark() {}

    public static Remark create(String entityType, UUID entityId, String remarkType, String content) {
        Remark r = new Remark();
        r.id = UUID.randomUUID();
        r.entityType = entityType;
        r.entityId = entityId;
        r.remarkType = remarkType;
        r.content = content;
        r.priority = "NORMAL";
        r.createdAt = java.time.Instant.now();
        return r;
    }
}
