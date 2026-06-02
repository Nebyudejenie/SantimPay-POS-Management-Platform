package com.santimpay.posctl.shared.security;

/**
 * Canonical {@code resource:action} permission constants. The authoritative catalogue also lives in
 * the database ({@code identity.permissions}) and is seeded by migration; this class exists so code
 * references are compile-checked. Keep the two in sync (a test asserts parity).
 */
public final class Permissions {

    private Permissions() {}

    // Merchant
    public static final String MERCHANT_READ = "merchant:read";
    public static final String MERCHANT_CREATE = "merchant:create";
    public static final String MERCHANT_UPDATE = "merchant:update";
    public static final String MERCHANT_APPROVE = "merchant:approve";

    // Device / inventory
    public static final String DEVICE_READ = "device:read";
    public static final String DEVICE_CREATE = "device:create";
    public static final String DEVICE_ASSIGN = "device:assign";
    public static final String DEVICE_RETIRE = "device:retire";

    // Deployment
    public static final String DEPLOYMENT_READ = "deployment:read";
    public static final String DEPLOYMENT_CREATE = "deployment:create";
    public static final String DEPLOYMENT_COMPLETE = "deployment:complete";

    // KYC
    public static final String KYC_READ = "kyc:read";
    public static final String KYC_REVIEW = "kyc:review";
    public static final String KYC_APPROVE = "kyc:approve";

    // Reporting & admin
    public static final String REPORT_READ = "report:read";
    public static final String REPORT_EXPORT = "report:export";
    public static final String TASK_ASSIGN = "task:assign";
    public static final String USER_MANAGE = "user:manage";
    public static final String PII_READ = "pii:read";
}
