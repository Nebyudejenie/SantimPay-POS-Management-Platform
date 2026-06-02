/**
 * Notifications — in-app/email/sms/push; owns the transactional outbox table.
 * Allowed dependency: {@code shared}. Integrates with other modules ONLY via published events.
 * Stub module — implement following the {@code merchant} reference (IMPLEMENTATION.md §7).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Notification")
package com.santimpay.posctl.notification;
