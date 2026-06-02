package com.santimpay.posctl.tasks.domain;

/** Provenance of a task — distinguishes human-created from event/AI/system-generated work. */
public enum TaskSource {
    MANUAL,
    AI,
    WORKFLOW,
    SYSTEM
}
