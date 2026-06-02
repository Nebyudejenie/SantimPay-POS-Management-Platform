/**
 * Published integration events of the merchant module — the ONLY merchant types other modules may
 * depend on. Declaring it a {@code @NamedInterface} is what makes that dependency legal under Spring
 * Modulith's {@code verify()} (sub-packages are module-internal by default).
 */
@org.springframework.modulith.NamedInterface("events")
package com.santimpay.posctl.merchant.events;
