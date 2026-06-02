/**
 * KYC bounded context — KYC requests and merchant KYC change requests with their own state machines.
 * Integrates with merchant via events (consumes {@code MerchantOnboarded}, publishes
 * {@code KycApproved}). Allowed dependency: {@code shared}.
 */
@org.springframework.modulith.ApplicationModule(displayName = "KYC")
package com.santimpay.posctl.kyc;
