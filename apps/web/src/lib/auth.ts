import { WebStorageStateStore } from "oidc-client-ts";
import type { AuthProviderProps } from "react-oidc-context";

/**
 * Keycloak OIDC (Authorization Code + PKCE). Access token kept in memory; refresh handled by the
 * oidc client. The token is attached to API requests by the Axios interceptor in `http.ts`.
 */
export const oidcConfig: AuthProviderProps = {
  authority: import.meta.env.VITE_OIDC_AUTHORITY,
  client_id: import.meta.env.VITE_OIDC_CLIENT_ID,
  redirect_uri: import.meta.env.VITE_OIDC_REDIRECT_URI,
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
