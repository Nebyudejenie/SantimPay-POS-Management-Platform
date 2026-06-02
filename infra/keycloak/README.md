# Keycloak realm — posctl

`realm-export.json` defines the **posctl** realm consumed by the backend (OIDC resource server),
the React SPA (`posctl-web`), and the Flutter app (`posctl-field`).

## Key design
- **Authentication** lives here; **authorization** is the `permissions` token claim.
- Fine-grained `resource:action` permissions are modeled as **client roles of `posctl-api`**, then
  mapped into the JWT `permissions` claim by the `posctl-permissions` client scope. This is exactly
  what `SecurityConfig` reads (`PERM_<resource:action>`) and what the DB seed mirrors.
- `uid` user attribute → `uid` claim → the app's internal `UserId` (`CurrentUser.id()`).
- Realm roles (`SUPER_ADMIN`, `OPS_MANAGER`, …) map to `ROLE_*` authorities; their permission
  composites are applied by `assign-role-permissions.sh` (kept out of the JSON because client-role
  composites don't import cleanly inline).

## Local dev
`docker-compose.dev.yml` already imports this file (`start-dev --import-realm`). Then:
```bash
KC_URL=http://localhost:8081 KC_ADMIN=admin KC_PW=admin ./assign-role-permissions.sh
```
Login: `admin@santimpay.com` / `changeme` (temporary; reset on first login). The admin user is
pre-granted every permission so you can exercise the full UI immediately.

## Production
Provisioned by Terraform (`infra/terraform/modules/keycloak-realm`) rather than dev import; this JSON
is the source of truth the TF module is generated from. Rotate the admin credential and disable the
temporary password.
