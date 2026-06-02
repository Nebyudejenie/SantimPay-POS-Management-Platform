/**
 * Published health events. Exposed as a {@code @NamedInterface} so tasks/followup/notification can
 * react (e.g. open a follow-up when a device goes offline).
 */
@org.springframework.modulith.NamedInterface("events")
package com.santimpay.posctl.health.events;
