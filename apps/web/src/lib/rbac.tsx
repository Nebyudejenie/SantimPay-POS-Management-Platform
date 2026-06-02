import { useAuth } from "react-oidc-context";
import { useMemo, type ReactNode } from "react";
import { permissionsFromToken } from "./auth";

/** Hook exposing the current user's fine-grained permission set. */
export function usePermissions(): Set<string> {
  const auth = useAuth();
  return useMemo(
    () => permissionsFromToken(auth.user?.access_token),
    [auth.user?.access_token],
  );
}

/**
 * Declarative permission gate. The server is still the enforcement point — this only controls UI
 * affordances (hide/disable). Usage: <Can permission="merchant:approve"><Button .../></Can>
 */
export function Can({
  permission,
  children,
  fallback = null,
}: {
  permission: string;
  children: ReactNode;
  fallback?: ReactNode;
}) {
  const perms = usePermissions();
  return <>{perms.has(permission) ? children : fallback}</>;
}
