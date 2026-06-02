#!/usr/bin/env bash
# Composites the posctl-api client-role permissions into each realm role, mirroring the DB RBAC seed
# (V1_0001/0003/0006-0008). Run AFTER importing realm-export.json. Idempotent.
#
# Usage: KC_URL=http://localhost:8081 KC_ADMIN=admin KC_PW=admin ./assign-role-permissions.sh
set -euo pipefail
: "${KC_URL:=http://localhost:8081}" "${KC_ADMIN:=admin}" "${KC_PW:=admin}"
REALM=posctl
API_CLIENT=posctl-api

kcadm() { /opt/keycloak/bin/kcadm.sh "$@"; }
kcadm config credentials --server "$KC_URL" --realm master --user "$KC_ADMIN" --password "$KC_PW"

declare -A ROLE_PERMS=(
  [OPS_MANAGER]="merchant:read merchant:create merchant:update merchant:approve device:read device:create device:update device:assign device:retire device:telemetry deployment:read deployment:create deployment:complete kyc:read report:read report:export task:read task:create task:update task:assign workflow:read workflow:approve"
  [CALL_CENTER]="merchant:read device:read deployment:read kyc:read task:read workflow:read followup:read followup:create"
  [FIELD_OFFICER]="merchant:read device:read device:update device:assign deployment:read deployment:create deployment:complete device:telemetry task:read task:update"
  [FINANCE]="merchant:read report:read report:export kyc:read kyc:approve"
  [COMPLIANCE]="merchant:read device:read deployment:read kyc:read kyc:review kyc:approve report:read workflow:read"
  [DATA_ENCODER]="merchant:read merchant:create merchant:update deployment:read deployment:create kyc:read followup:create"
  [IT_ADMIN]="device:read deployment:read report:read"
)

for role in "${!ROLE_PERMS[@]}"; do
  for perm in ${ROLE_PERMS[$role]}; do
    kcadm add-roles -r "$REALM" --rname "$role" --cclientid "$API_CLIENT" --rolename "$perm" 2>/dev/null \
      && echo "  $role += $perm" || true
  done
done
echo "Done. SUPER_ADMIN gets all permissions directly on the admin user (see realm export)."
