/**
 * Published approval events. Exposed as a {@code @NamedInterface} so subject-owning modules (e.g.
 * merchant) can react to {@code ApprovalGranted} for their subject without importing workflow
 * internals.
 */
@org.springframework.modulith.NamedInterface("events")
package com.santimpay.posctl.workflow.events;
