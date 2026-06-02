package com.santimpay.posctl.identity.application;

import java.util.Set;
import java.util.UUID;

/**
 * The current principal as the frontends need it: identity + effective permissions (the web RBAC
 * gate and the Flutter app both call {@code GET /me} on load).
 */
public record MeView(
        UUID userId,
        String email,
        String fullName,
        Set<String> roles,
        Set<String> permissions) {}
