package com.santimpay.posctl.workflow.application;

import java.util.UUID;
import org.springframework.modulith.NamedInterface;

/**
 * Published application API for starting an approval workflow. Exposed as a {@code @NamedInterface}
 * so other modules (e.g. kyc) can request an approval gate without depending on workflow internals.
 * This is the deliberate, narrow way to invoke a cross-module command (as opposed to fire-and-forget
 * events) when a synchronous "create this gate now" semantic is wanted.
 *
 * <p>The workflow type is passed as a {@code String} (one of the {@code WorkflowType} names) — NOT
 * the {@code workflow.domain.WorkflowType} enum — precisely so callers never import workflow's
 * internals. The service validates it against the enum. This keeps module boundaries clean (a
 * cross-module {@code domain} import would fail {@code ModularityTests}).
 */
@NamedInterface("api")
public interface WorkflowInitiation {

    /**
     * Start a single-step approval over a subject; returns the new instance id.
     *
     * @param type one of {@code WorkflowType} names, e.g. {@code "MERCHANT_ACTIVATION"}
     */
    UUID start(String type, String subjectType, UUID subjectId, UUID initiatedBy);
}
