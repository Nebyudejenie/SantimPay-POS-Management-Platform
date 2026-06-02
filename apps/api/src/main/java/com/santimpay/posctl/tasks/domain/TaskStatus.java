package com.santimpay.posctl.tasks.domain;

/**
 * Task lifecycle. Transitions enforced by {@link Task}:
 * <pre>
 *   OPEN        -> ASSIGNED | CANCELLED
 *   ASSIGNED    -> IN_PROGRESS | CANCELLED
 *   IN_PROGRESS -> BLOCKED | DONE | CANCELLED
 *   BLOCKED     -> IN_PROGRESS | CANCELLED
 * </pre>
 */
public enum TaskStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    BLOCKED,
    DONE,
    CANCELLED
}
