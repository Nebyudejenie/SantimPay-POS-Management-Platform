package com.santimpay.posctl.identity.web;

import com.santimpay.posctl.identity.application.RolePermissions;
import com.santimpay.posctl.identity.infrastructure.KeycloakAdminClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.security.SecureRandom;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin endpoints for the Super Admin to manage users + roles from inside the app. Guarded by
 * {@code user:manage}. Creating a user provisions it in Keycloak (the IdP) with a one-time temporary
 * password the user must reset on first login — production-grade, no plaintext password handling.
 */
@Tag(name = "Admin")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final KeycloakAdminClient keycloak;
    private static final SecureRandom RANDOM = new SecureRandom();

    public record CreateUserRequest(
            @NotBlank @Email String email,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String role) {}

    public record CreatedUser(String userId, String username, String role, int permissions,
                              String temporaryPassword) {}

    public record RoleInfo(String role, int permissions, List<String> permissionList) {}

    @Operation(summary = "List assignable roles and their permission presets")
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('PERM_user:manage')")
    public List<RoleInfo> roles() {
        return RolePermissions.roles().stream()
                .map(r -> new RoleInfo(r, RolePermissions.forRole(r).size(), RolePermissions.forRole(r)))
                .toList();
    }

    @Operation(summary = "Create a user with a role (provisions in Keycloak, temp password on first login)")
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('PERM_user:manage')")
    public CreatedUser createUser(@Valid @RequestBody CreateUserRequest req) {
        List<String> perms = RolePermissions.forRole(req.role());
        if (perms.isEmpty()) {
            throw new IllegalArgumentException("Unknown role: " + req.role());
        }
        String tempPw = generateTempPassword();
        String id = keycloak.createUser(req.email(), req.email(), req.firstName(), req.lastName(),
                req.role(), perms, tempPw);
        // The temp password is returned ONCE so the admin can hand it to the user; it's not stored.
        return new CreatedUser(id, req.email(), req.role(), perms.size(), tempPw);
    }

    private String generateTempPassword() {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder("Posctl-");
        for (int i = 0; i < 8; i++) sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        return sb.toString();
    }
}
