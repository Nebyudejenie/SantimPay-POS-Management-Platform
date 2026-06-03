// Runtime configuration for the SPA. Served as a static file by nginx, so it can be edited (or
// volume-mounted) WITHOUT rebuilding the image. Vite would otherwise bake VITE_* vars at build time.
// Defaults are derived from the browser's current host so one image works on localhost or any LAN IP.
(function () {
  // Single-origin: the SPA, the API (/api) and Keycloak (/auth) are all proxied under THIS origin
  // (https). Deriving from window.location.origin keeps it secure-context + same-origin (no
  // mixed-content, crypto.subtle available for PKCE).
  var origin = window.location.origin; // e.g. https://192.168.1.40:8443
  window.__POSCTL_CONFIG__ = {
    OIDC_AUTHORITY: origin + "/auth/realms/posctl",
    OIDC_CLIENT_ID: "posctl-web",
    OIDC_REDIRECT_URI: origin + "/",
    API_BASE_URL: origin + "/api/v1",
  };
})();
