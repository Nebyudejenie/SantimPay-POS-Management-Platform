/**
 * Published integration events of the deployment module (the module's public contract). Exposed as a
 * {@code @NamedInterface} so e.g. inventory may react to {@code DeviceAssigned} without importing
 * deployment internals.
 */
@org.springframework.modulith.NamedInterface("events")
package com.santimpay.posctl.deployment.events;
