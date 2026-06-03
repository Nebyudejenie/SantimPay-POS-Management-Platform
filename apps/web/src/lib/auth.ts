import { WebStorageStateStore } from "oidc-client-ts";
import type { AuthProviderProps } from "react-oidc-context";

/** Runtime config injected by /config.js (window.__POSCTL_CONFIG__), with build-time + sane fallbacks. */
interface RuntimeConfig {
  OIDC_AUTHORITY?: string;
  OIDC_CLIENT_ID?: string;
  OIDC_REDIRECT_URI?: string;
  API_BASE_URL?: string;
}
const rc: RuntimeConfig =
  (typeof window !== "undefined" &&
    (window as unknown as { __POSCTL_CONFIG__?: RuntimeConfig }).__POSCTL_CONFIG__) ||
  {};

export const runtimeConfig = {
  apiBaseUrl: rc.API_BASE_URL ?? import.meta.env.VITE_API_BASE_URL ?? "/api/v1",
  oidcAuthority: rc.OIDC_AUTHORITY ?? import.meta.env.VITE_OIDC_AUTHORITY ?? "",
  oidcClientId: rc.OIDC_CLIENT_ID ?? import.meta.env.VITE_OIDC_CLIENT_ID ?? "posctl-web",
  oidcRedirectUri:
    rc.OIDC_REDIRECT_URI ?? import.meta.env.VITE_OIDC_REDIRECT_URI ?? `${window.location.origin}/`,
};

/**
 * Keycloak OIDC (Authorization Code + PKCE). Access token kept in memory; refresh handled by the
 * oidc client. The token is attached to API requests by the Axios interceptor in `http.ts`.
 */
export const oidcConfig: AuthProviderProps = {
  authority: runtimeConfig.oidcAuthority,
  client_id: runtimeConfig.oidcClientId,
  redirect_uri: runtimeConfig.oidcRedirectUri,
  response_type: "code",
  scope: "openid profile email",
  automaticSilentRenew: true,
  userStore: new WebStorageStateStore({ store: window.localStorage }),
  onSigninCallback: () => {
    window.history.replaceState({}, document.title, window.location.pathname);
  },
};

/** Extract fine-grained permissions from the decoded access token (`permissions` claim). */
export function permissionsFromToken(accessToken?: string): Set<string> {
  if (!accessToken) return new Set();
  try {
    const payload = JSON.parse(atob(accessToken.split(".")[1]));
    return new Set<string>(payload.permissions ?? []);
  } catch {
    return new Set();
  }
}
