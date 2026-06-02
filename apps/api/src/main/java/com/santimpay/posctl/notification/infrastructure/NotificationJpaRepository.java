package com.santimpay.posctl.notification.infrastructure;

import com.santimpay.posctl.notification.domain.Notification;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {

    @Query("""
           select n from Notification n
           where n.recipientId = :recipientId
             and (:unreadOnly = false or n.status <> com.santimpay.posctl.notification.domain.NotificationStatus.READ)
           order by n.audit.createdAt desc
           """)
    Page<Notification> findForRecipient(@Param("recipientId") UUID recipientId,
                                        @Param("unreadOnly") boolean unreadOnly,
                                        Pageable pageable);
}
