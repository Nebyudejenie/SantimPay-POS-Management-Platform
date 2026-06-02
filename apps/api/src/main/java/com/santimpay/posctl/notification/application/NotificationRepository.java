package com.santimpay.posctl.notification.application;

import com.santimpay.posctl.notification.domain.Notification;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    Page<Notification> findForRecipient(UUID recipientId, boolean unreadOnly, Pageable pageable);
}
