package com.santimpay.posctl.notification.application;

import com.santimpay.posctl.notification.domain.Notification;
import com.santimpay.posctl.shared.domain.DomainException;
import com.santimpay.posctl.shared.security.CurrentUser;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Notifications use cases. Implements {@link NotificationCreation} (system/event-driven) and the
 * user-facing inbox (list mine, mark read). New in-app notifications are also pushed live over SSE.
 */
@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationCreation {

    private final NotificationRepository repository;
    private final NotificationStream stream;

    @Override
    @Transactional
    public UUID notifyInApp(UUID recipientId, String template, String payloadJson,
                            String relatedType, UUID relatedId) {
        Notification n = repository.save(
                Notification.inApp(recipientId, template, payloadJson, relatedType, relatedId));
        stream.push(recipientId, "notification", java.util.Map.of(
                "id", n.getId().toString(), "template", template,
                "relatedType", relatedType == null ? "" : relatedType));
        return n.getId();
    }

    @Transactional(readOnly = true)
    public Page<Notification> myInbox(boolean unreadOnly, Pageable pageable) {
        UUID me = CurrentUser.id().orElseThrow(() -> DomainException.notFound("User", "current"));
        return repository.findForRecipient(me, unreadOnly, pageable);
    }

    @Transactional
    public Notification markRead(UUID id) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> DomainException.notFound("Notification", id));
        n.markRead();
        return repository.save(n);
    }
}
