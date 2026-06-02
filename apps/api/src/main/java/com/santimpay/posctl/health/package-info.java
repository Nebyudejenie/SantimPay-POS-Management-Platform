/**
 * Device Health Monitoring — high-volume telemetry ingest; publishes DeviceOfflineDetected.
 * Allowed dependency: {@code shared}. Integrates with other modules ONLY via published events.
 * Stub module — implement following the {@code merchant} reference (IMPLEMENTATION.md §7).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Health")
package com.santimpay.posctl.health;
