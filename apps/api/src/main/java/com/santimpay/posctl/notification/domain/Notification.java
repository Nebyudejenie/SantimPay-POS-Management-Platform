package com.santimpay.posctl.notification.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A notification addressed to a user (or the ops inbox when {@code recipientId} is null). The
 * delivery channel adapters (email/sms/push) consume PENDING rows; in-app notifications are read via
 * the API + SSE stream. {@code payload} is a JSON blob the UI renders against the named template.
 */
@Getter
@Entity
@Table(name = "notifications", schema = "notification")
public class Notification extends AggregateRoot<Notification> {

    @Column(name = "recipient_id")
    private UUID recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    @Column(name = "template", nullable = false)
    private String template;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    @Column(name = "related_type")
    private String relatedType;

    @Column(name = "related_id")
    private UUID relatedId;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "read_at")
    private Instant readAt;

    protected Notification() {}

    public static Notification inApp(UUID recipientId, String template, String payloadJson,
                                     String relatedType, UUID relatedId) {
        Notification n = new Notification();
        n.assignIdentityIfAbsent();
        n.recipientId = recipientId;
        n.channel = NotificationChannel.IN_APP;
        n.template = template;
        n.payload = payloadJson == null ? "{}" : payloadJson;
        n.status = NotificationStatus.PENDING;
        n.relatedType = relatedType;
        n.relatedId = relatedId;
        return n;
    }

    public void markRead() {
        this.status = NotificationStatus.READ;
        this.readAt = Instant.now();
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
    }
}
