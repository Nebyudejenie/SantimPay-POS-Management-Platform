package com.santimpay.posctl.notification.infrastructure;

import com.santimpay.posctl.notification.application.NotificationRepository;
import com.santimpay.posctl.notification.domain.Notification;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpa;

    @Override
    public Notification save(Notification notification) {
        return jpa.save(notification);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Page<Notification> findForRecipient(UUID recipientId, boolean unreadOnly, Pageable pageable) {
        return jpa.findForRecipient(recipientId, unreadOnly, pageable);
    }
}
