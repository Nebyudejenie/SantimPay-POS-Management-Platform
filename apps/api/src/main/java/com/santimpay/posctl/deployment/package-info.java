/**
 * Deployment — daily deployments & temporal device assignments. Consumes MerchantActivated.
 * Allowed dependency: {@code shared}. Integrates with other modules ONLY via published events.
 * Stub module — implement following the {@code merchant} reference (IMPLEMENTATION.md §7).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Deployment")
package com.santimpay.posctl.deployment;
