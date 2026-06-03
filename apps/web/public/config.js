// Runtime configuration for the SPA. Served as a static file by nginx, so it can be edited (or
// volume-mounted) WITHOUT rebuilding the image. Vite would otherwise bake VITE_* vars at build time.
// Defaults are derived from the browser's current host so one image works on localhost or any LAN IP.
(function () {
  var host = window.location.hostname; // e.g. 192.168.1.40
  window.__POSCTL_CONFIG__ = {
    // Keycloak realm issuer (Keycloak runs on :8081 on the same host in the single-VM deploy).
    OIDC_AUTHORITY: "http://" + host + ":8081/realms/posctl",
    OIDC_CLIENT_ID: "posctl-web",
    OIDC_REDIRECT_URI: window.location.origin + "/",
    // API base — same host, port 8080.
    API_BASE_URL: "http://" + host + ":8080/api/v1",
  };
})();
