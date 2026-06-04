package com.santimpay.posctl.identity.application;

import java.util.List;
import java.util.Map;

/**
 * The role → permission presets (mirrors docs/10 RBAC matrix). When a Super Admin creates a user and
 * picks a role, these are the {@code resource:action} permissions granted in Keycloak. Keeping the
 * mapping here means the UI only needs to choose a role, not hand-pick permissions.
 */
public final class RolePermissions {

    private RolePermissions() {}

    public static final List<String> ALL = List.of(
            "merchant:read","merchant:create","merchant:update","merchant:approve",
            "device:read","device:create","device:update","device:assign","device:retire","device:telemetry",
            "deployment:read","deployment:create","deployment:complete",
            "kyc:read","kyc:review","kyc:approve","workflow:read","workflow:approve",
            "task:read","task:create","task:update","task:assign","followup:read","followup:create",
            "report:read","report:export","user:manage","pii:read");

    public static final Map<String, List<String>> PRESETS = Map.of(
            "SUPER_ADMIN", ALL,
            "OPS_MANAGER", List.of("merchant:read","merchant:create","merchant:update","merchant:approve",
                    "device:read","device:create","device:update","device:assign","device:retire","device:telemetry",
                    "deployment:read","deployment:create","deployment:complete","kyc:read","report:read","report:export",
                    "task:read","task:create","task:update","task:assign","workflow:read","workflow:approve",
                    "followup:read","followup:create"),
            "CALL_CENTER", List.of("merchant:read","device:read","deployment:read","kyc:read","task:read","task:update",
                    "workflow:read","followup:read","followup:create"),
            "FIELD_OFFICER", List.of("merchant:read","device:read","device:assign","device:update","device:telemetry",
                    "deployment:read","deployment:create","deployment:complete","task:read","task:update"),
            "FINANCE", List.of("merchant:read","report:read","report:export","kyc:read","kyc:approve"),
            "COMPLIANCE", List.of("merchant:read","device:read","deployment:read","kyc:read","kyc:review","kyc:approve",
                    "report:read","workflow:read"),
            "DATA_ENCODER", List.of("merchant:read","merchant:create","merchant:update","deployment:read",
                    "deployment:create","kyc:read","followup:create","task:read"),
            "IT_ADMIN", List.of("device:read","deployment:read","report:read","user:manage"));

    public static List<String> forRole(String role) {
        return PRESETS.getOrDefault(role, List.of());
    }

    public static List<String> roles() {
        return List.of("SUPER_ADMIN","OPS_MANAGER","CALL_CENTER","FIELD_OFFICER",
                "FINANCE","COMPLIANCE","DATA_ENCODER","IT_ADMIN");
    }
}
