-- ============================================================================
-- V1_0001 identity: users, roles, permissions, employees + RBAC seed.
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE identity.users (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  keycloak_sub  text UNIQUE,
  email         citext NOT NULL,
  full_name     text NOT NULL,
  phone         text,
  status        text NOT NULL DEFAULT 'active' CHECK (status IN ('active','suspended','disabled')),
  last_login_at timestamptz,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  created_by    uuid, updated_by uuid,
  version       integer NOT NULL DEFAULT 0,
  deleted_at    timestamptz
);
CREATE UNIQUE INDEX ux_users_email_live ON identity.users(email) WHERE deleted_at IS NULL;

CREATE TABLE identity.roles (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  code        text UNIQUE NOT NULL,
  name        text NOT NULL,
  description text,
  is_system   boolean NOT NULL DEFAULT false,
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now(),
  version     integer NOT NULL DEFAULT 0,
  deleted_at  timestamptz
);

CREATE TABLE identity.permissions (
  id        uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  resource  text NOT NULL,
  action    text NOT NULL,
  UNIQUE (resource, action)
);

CREATE TABLE identity.role_permissions (
  role_id       uuid NOT NULL REFERENCES identity.roles(id) ON DELETE CASCADE,
  permission_id uuid NOT NULL REFERENCES identity.permissions(id) ON DELETE CASCADE,
  PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE identity.user_roles (
  user_id uuid NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
  role_id uuid NOT NULL REFERENCES identity.roles(id) ON DELETE CASCADE,
  granted_at timestamptz NOT NULL DEFAULT now(),
  granted_by uuid,
  PRIMARY KEY (user_id, role_id)
);

CREATE TABLE identity.employees (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     uuid UNIQUE REFERENCES identity.users(id) ON DELETE SET NULL,
  employee_no text UNIQUE NOT NULL,
  department  text, job_title text, region text,
  manager_id  uuid REFERENCES identity.employees(id),
  hired_at    date,
  status      text NOT NULL DEFAULT 'active' CHECK (status IN ('active','on_leave','terminated')),
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now(),
  version     integer NOT NULL DEFAULT 0,
  deleted_at  timestamptz
);

-- ---- seed permission catalogue (kept in sync with shared.security.Permissions) ----
INSERT INTO identity.permissions(resource, action) VALUES
  ('merchant','read'),('merchant','create'),('merchant','update'),('merchant','approve'),
  ('device','read'),('device','create'),('device','assign'),('device','retire'),('device','telemetry'),
  ('deployment','read'),('deployment','create'),('deployment','complete'),
  ('kyc','read'),('kyc','review'),('kyc','approve'),
  ('report','read'),('report','export'),
  ('task','read'),('task','assign'),('user','manage'),('pii','read')
ON CONFLICT DO NOTHING;

-- ---- seed primary operational roles ----
INSERT INTO identity.roles(code, name, is_system, description) VALUES
  ('SUPER_ADMIN','Super Admin', true, 'Full system + user administration'),
  ('OPS_MANAGER','Operations Manager', true, 'Inventory, deployments, approvals, reporting'),
  ('CALL_CENTER','Call Center Agent', true, 'Follow-ups, KYC intake, lookups'),
  ('FIELD_OFFICER','Field Deployment Officer', true, 'Field deployments (region-scoped)')
ON CONFLICT DO NOTHING;

-- Super Admin gets everything.
INSERT INTO identity.role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r CROSS JOIN identity.permissions p
WHERE r.code = 'SUPER_ADMIN' ON CONFLICT DO NOTHING;

-- Ops Manager: broad operational + approvals (no user mgmt).
INSERT INTO identity.role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r JOIN identity.permissions p
  ON (p.resource, p.action) IN (
     ('merchant','read'),('merchant','create'),('merchant','update'),('merchant','approve'),
     ('device','read'),('device','create'),('device','assign'),('device','retire'),
     ('deployment','read'),('deployment','create'),('deployment','complete'),
     ('kyc','read'),('report','read'),('report','export'),('task','read'),('task','assign'))
WHERE r.code = 'OPS_MANAGER' ON CONFLICT DO NOTHING;

-- Call Center: read + follow-up/KYC intake.
INSERT INTO identity.role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r JOIN identity.permissions p
  ON (p.resource, p.action) IN (
     ('merchant','read'),('device','read'),('deployment','read'),
     ('kyc','read'),('task','read'))
WHERE r.code = 'CALL_CENTER' ON CONFLICT DO NOTHING;

-- Field Officer: deployment + device telemetry, region-scoped at app layer.
INSERT INTO identity.role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r JOIN identity.permissions p
  ON (p.resource, p.action) IN (
     ('merchant','read'),('device','read'),('device','assign'),
     ('deployment','read'),('deployment','create'),('deployment','complete'),
     ('device','telemetry'))
WHERE r.code = 'FIELD_OFFICER' ON CONFLICT DO NOTHING;
