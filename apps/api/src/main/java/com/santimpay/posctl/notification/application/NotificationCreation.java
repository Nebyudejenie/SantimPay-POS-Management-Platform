package com.santimpay.posctl.notification.application;

import java.util.UUID;
import org.springframework.modulith.NamedInterface;

/**
 * Published API for emitting notifications from other modules / system contexts. Exposed as a
 * {@code @NamedInterface}. {@code recipientId == null} means the ops/team inbox (broadcast).
 */
@NamedInterface("api")
public interface NotificationCreation {

    UUID notifyInApp(UUID recipientId, String template, String payloadJson,
                     String relatedType, UUID relatedId);
}
