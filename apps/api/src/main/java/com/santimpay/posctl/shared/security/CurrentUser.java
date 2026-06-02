package com.santimpay.posctl.shared.security;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Convenience accessor for the authenticated principal derived from the Keycloak JWT.
 * The internal user id is carried in the {@code uid} claim (provisioned by the identity module);
 * we fall back to {@code sub} when {@code uid} is absent (e.g. service accounts).
 */
public final class CurrentUser {

    private CurrentUser() {}

    public static Optional<UUID> id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }
        String raw = jwt.hasClaim("uid") ? jwt.getClaimAsString("uid") : jwt.getSubject();
        try {
            return Optional.ofNullable(raw).map(UUID::fromString);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static Set<String> permissions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Set.of();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("PERM_"))
                .map(a -> a.substring("PERM_".length()))
                .collect(Collectors.toSet());
    }

    public static boolean hasPermission(String permission) {
        return permissions().contains(permission);
    }
}
