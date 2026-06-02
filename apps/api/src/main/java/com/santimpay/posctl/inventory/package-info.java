/**
 * Inventory — POS devices, SIM cards, banks. Publishes DeviceReceived, DeviceMarkedFaulty.
 * Allowed dependency: {@code shared}. Integrates with other modules ONLY via published events.
 * Stub module — implement following the {@code merchant} reference (IMPLEMENTATION.md §7).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Inventory")
package com.santimpay.posctl.inventory;
