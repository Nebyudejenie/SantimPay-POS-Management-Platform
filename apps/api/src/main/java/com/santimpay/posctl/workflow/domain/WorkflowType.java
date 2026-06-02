package com.santimpay.posctl.workflow.domain;

/** Kinds of approval the generic engine drives. New gates are added here, not as new modules. */
public enum WorkflowType {
    MERCHANT_ACTIVATION,
    DEVICE_WRITE_OFF,
    KYC_CHANGE
}
