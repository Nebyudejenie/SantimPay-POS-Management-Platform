package com.santimpay.posctl.inventory.domain;

/**
 * POS device lifecycle (see docs/02 §4.5). Transitions are enforced by {@link PosDevice}; no other
 * code may set this field. Allowed transitions:
 * <pre>
 *   IN_STOCK   -> ALLOCATED | LOST
 *   ALLOCATED  -> DEPLOYED | IN_STOCK
 *   DEPLOYED   -> FAULTY | IN_STOCK | RETIRED | LOST
 *   FAULTY     -> IN_REPAIR | RETIRED
 *   IN_REPAIR  -> IN_STOCK | RETIRED
 * </pre>
 */
public enum DeviceStatus {
    IN_STOCK,
    ALLOCATED,
    DEPLOYED,
    FAULTY,
    IN_REPAIR,
    RETIRED,
    LOST
}
