-- =============================================================================
-- SantimPay POS Management Platform (posctl) — PostgreSQL Schema
-- PostgreSQL 16+. Schema-per-module (modular monolith). UUIDv7 PKs.
-- Soft deletes, optimistic locking, generic audit, history tables,
-- partitioning, materialized/reporting views, transactional outbox.
-- This is a representative production baseline, not exhaustive of every column.
-- =============================================================================

-- ---------- Extensions ----------
CREATE EXTENSION IF NOT EXISTS pgcrypto;          -- gen_random_uuid, digest
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS btree_gist;        -- exclusion constraints
CREATE EXTENSION IF NOT EXISTS pg_trgm;           -- fuzzy search
CREATE EXTENSION IF NOT EXISTS vector;            -- pgvector for AI
-- pg_partman / pg_cron installed at cluster level for partitioning & scheduling.

-- ---------- Schemas ----------
CREATE SCHEMA IF NOT EXISTS shared;
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS merchant;
CREATE SCHEMA IF NOT EXISTS inventory;
CREATE SCHEMA IF NOT EXISTS deployment;
CREATE SCHEMA IF NOT EXISTS kyc;
CREATE SCHEMA IF NOT EXISTS tasks;
CREATE SCHEMA IF NOT EXISTS workflow;
CREATE SCHEMA IF NOT EXISTS followup;
CREATE SCHEMA IF NOT EXISTS analytics;
CREATE SCHEMA IF NOT EXISTS health;
CREATE SCHEMA IF NOT EXISTS notification;
CREATE SCHEMA IF NOT EXISTS ai;
CREATE SCHEMA IF NOT EXISTS audit;

-- =============================================================================
-- UUIDv7 generator (time-ordered) — better index locality than v4
-- =============================================================================
CREATE OR REPLACE FUNCTION shared.uuid_generate_v7()
RETURNS uuid AS $$
DECLARE
  unix_ts_ms bytea;
  uuid_bytes bytea;
BEGIN
  unix_ts_ms := substring(int8send((extract(epoch FROM clock_timestamp()) * 1000)::bigint) FROM 3);
  uuid_bytes := unix_ts_ms || gen_random_bytes(10);
  uuid_bytes := set_byte(uuid_bytes, 6, (b'0111' || get_byte(uuid_bytes, 6)::bit(4))::bit(8)::int);
  uuid_bytes := set_byte(uuid_bytes, 8, (b'10'  || get_byte(uuid_bytes, 8)::bit(6))::bit(8)::int);
  RETURN encode(uuid_bytes, 'hex')::uuid;
END;
$$ LANGUAGE plpgsql VOLATILE;

-- =============================================================================
-- Shared triggers: updated_at + optimistic version bump
-- =============================================================================
CREATE OR REPLACE FUNCTION shared.tg_touch_row()
RETURNS trigger AS $$
BEGIN
  NEW.updated_at := now();
  IF TG_OP = 'UPDATE' THEN
    NEW.version := COALESCE(OLD.version, 0) + 1;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Generic audit trigger -> audit.audit_log (defined after audit_log below)

-- =============================================================================
-- AUDIT (partitioned, immutable)
-- =============================================================================
CREATE TABLE audit.audit_log (
  id           uuid           NOT NULL DEFAULT shared.uuid_generate_v7(),
  occurred_at  timestamptz    NOT NULL DEFAULT now(),
  schema_name  text           NOT NULL,
  table_name   text           NOT NULL,
  row_id       uuid,
  action       text           NOT NULL CHECK (action IN ('INSERT','UPDATE','DELETE')),
  actor_id     uuid,                       -- identity.users.id (no FK: cross-schema)
  actor_label  text,
  old_data     jsonb,
  new_data     jsonb,
  diff         jsonb,
  request_id   text,                       -- correlation id from API
  PRIMARY KEY (id, occurred_at)
) PARTITION BY RANGE (occurred_at);
-- Default partition + monthly partitions managed by pg_partman.
CREATE TABLE audit.audit_log_default PARTITION OF audit.audit_log DEFAULT;
CREATE INDEX ix_audit_table_row ON audit.audit_log (schema_name, table_name, row_id);
CREATE INDEX ix_audit_actor     ON audit.audit_log (actor_id);
-- Revoke UPDATE/DELETE at role level to enforce immutability (done in roles.sql).

CREATE OR REPLACE FUNCTION shared.tg_audit()
RETURNS trigger AS $$
DECLARE
  v_old jsonb := CASE WHEN TG_OP <> 'INSERT' THEN to_jsonb(OLD) END;
  v_new jsonb := CASE WHEN TG_OP <> 'DELETE' THEN to_jsonb(NEW) END;
BEGIN
  INSERT INTO audit.audit_log(schema_name, table_name, row_id, action, actor_id, old_data, new_data, diff, request_id)
  VALUES (
    TG_TABLE_SCHEMA, TG_TABLE_NAME,
    COALESCE((v_new->>'id')::uuid, (v_old->>'id')::uuid),
    TG_OP,
    NULLIF(current_setting('app.actor_id', true),'')::uuid,
    v_old, v_new,
    CASE WHEN TG_OP='UPDATE' THEN (SELECT jsonb_object_agg(key, value)
         FROM jsonb_each(v_new) WHERE v_new->key IS DISTINCT FROM v_old->key) END,
    NULLIF(current_setting('app.request_id', true),'')
  );
  RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Helper to attach standard triggers to a table
-- (applied per-table below via DO blocks for brevity in real migrations).

-- =============================================================================
-- IDENTITY
-- =============================================================================
CREATE TABLE identity.users (
  id            uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  keycloak_sub  text UNIQUE,                    -- OIDC subject from Keycloak
  email         citext UNIQUE NOT NULL,
  full_name     text NOT NULL,
  phone         text,
  status        text NOT NULL DEFAULT 'active' CHECK (status IN ('active','suspended','disabled')),
  last_login_at timestamptz,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  created_by    uuid,
  updated_by    uuid,
  version       integer NOT NULL DEFAULT 0,
  deleted_at    timestamptz
);
CREATE UNIQUE INDEX ux_users_email_live ON identity.users(email) WHERE deleted_at IS NULL;

CREATE TABLE identity.roles (
  id          uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  code        text UNIQUE NOT NULL,             -- e.g. 'ADMIN','FIELD_AGENT','CALL_CENTER'
  name        text NOT NULL,
  description text,
  is_system   boolean NOT NULL DEFAULT false,
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now(),
  version     integer NOT NULL DEFAULT 0,
  deleted_at  timestamptz
);

CREATE TABLE identity.permissions (
  id        uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  resource  text NOT NULL,                      -- e.g. 'merchant','device','deployment'
  action    text NOT NULL,                      -- e.g. 'read','create','approve','export'
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
  id            uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  user_id       uuid UNIQUE REFERENCES identity.users(id) ON DELETE SET NULL,
  employee_no   text UNIQUE NOT NULL,
  department    text,
  job_title     text,
  manager_id    uuid REFERENCES identity.employees(id),
  region        text,
  hired_at      date,
  status        text NOT NULL DEFAULT 'active' CHECK (status IN ('active','on_leave','terminated')),
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  version       integer NOT NULL DEFAULT 0,
  deleted_at    timestamptz
);

-- =============================================================================
-- MERCHANT
-- =============================================================================
CREATE TABLE merchant.merchants (
  id           uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  merchant_no  text UNIQUE NOT NULL,
  legal_name   text NOT NULL,
  trade_name   text,
  tax_id       text,                            -- TIN
  category     text,                            -- MCC / business category
  status       text NOT NULL DEFAULT 'onboarding'
               CHECK (status IN ('onboarding','pending_kyc','active','suspended','closed')),
  risk_tier    text CHECK (risk_tier IN ('low','medium','high')),
  onboarded_at timestamptz,
  activated_at timestamptz,
  created_at   timestamptz NOT NULL DEFAULT now(),
  updated_at   timestamptz NOT NULL DEFAULT now(),
  created_by   uuid, updated_by uuid,
  version      integer NOT NULL DEFAULT 0,
  deleted_at   timestamptz
);
CREATE INDEX ix_merchants_status ON merchant.merchants(status) WHERE deleted_at IS NULL;
CREATE INDEX ix_merchants_name_trgm ON merchant.merchants USING gin (legal_name gin_trgm_ops);

CREATE TABLE merchant.merchant_owners (
  id           uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  merchant_id  uuid NOT NULL REFERENCES merchant.merchants(id) ON DELETE CASCADE,
  full_name    text NOT NULL,
  national_id  text,
  phone        text,
  email        citext,
  ownership_pct numeric(5,2) CHECK (ownership_pct BETWEEN 0 AND 100),
  is_primary   boolean NOT NULL DEFAULT false,
  created_at   timestamptz NOT NULL DEFAULT now(),
  updated_at   timestamptz NOT NULL DEFAULT now(),
  version      integer NOT NULL DEFAULT 0,
  deleted_at   timestamptz
);
CREATE INDEX ix_owners_merchant ON merchant.merchant_owners(merchant_id);

CREATE TABLE merchant.branches (
  id           uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  merchant_id  uuid NOT NULL REFERENCES merchant.merchants(id) ON DELETE CASCADE,
  branch_no    text NOT NULL,
  name         text NOT NULL,
  region       text, city text, sub_city text, woreda text,
  address_line text,
  latitude     numeric(9,6),
  longitude    numeric(9,6),
  contact_phone text,
  status       text NOT NULL DEFAULT 'active' CHECK (status IN ('active','inactive','closed')),
  created_at   timestamptz NOT NULL DEFAULT now(),
  updated_at   timestamptz NOT NULL DEFAULT now(),
  version      integer NOT NULL DEFAULT 0,
  deleted_at   timestamptz,
  UNIQUE (merchant_id, branch_no)
);
CREATE INDEX ix_branches_merchant ON merchant.branches(merchant_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_branches_geo ON merchant.branches(latitude, longitude);

CREATE TABLE inventory.banks (
  id        uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  code      text UNIQUE NOT NULL,
  name      text NOT NULL,
  swift     text,
  is_active boolean NOT NULL DEFAULT true
);

CREATE TABLE merchant.settlement_accounts (
  id           uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  merchant_id  uuid NOT NULL REFERENCES merchant.merchants(id) ON DELETE CASCADE,
  bank_id      uuid NOT NULL REFERENCES inventory.banks(id),
  account_no   text NOT NULL,
  account_name text NOT NULL,
  currency     text NOT NULL DEFAULT 'ETB',
  is_primary   boolean NOT NULL DEFAULT false,
  verified_at  timestamptz,
  created_at   timestamptz NOT NULL DEFAULT now(),
  updated_at   timestamptz NOT NULL DEFAULT now(),
  version      integer NOT NULL DEFAULT 0,
  deleted_at   timestamptz,
  UNIQUE (merchant_id, bank_id, account_no)
);

-- =============================================================================
-- INVENTORY (devices & SIMs)
-- =============================================================================
CREATE TABLE inventory.sim_cards (
  id          uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  msisdn      text UNIQUE NOT NULL,
  iccid       text UNIQUE,
  carrier     text,
  status      text NOT NULL DEFAULT 'in_stock'
              CHECK (status IN ('in_stock','active','suspended','deactivated')),
  data_plan   text,
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now(),
  version     integer NOT NULL DEFAULT 0,
  deleted_at  timestamptz
);

CREATE TABLE inventory.pos_devices (
  id            uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  serial_no     text UNIQUE NOT NULL,          -- POS SERIAL NUMBER
  terminal_id   text UNIQUE,                   -- POS Terminal ID (TID)
  imei          text UNIQUE,
  model         text NOT NULL,                 -- Device Model
  vendor        text,                          -- Manufacturer
  firmware      text,
  qr_code       text,                          -- QR
  kcv           text,                          -- KCV
  combined_kcv  text,                          -- Combined KCV
  status        text NOT NULL DEFAULT 'in_stock'
                CHECK (status IN ('in_stock','allocated','deployed','faulty','in_repair','retired','lost')),
  active_sim_id uuid REFERENCES inventory.sim_cards(id),
  production_date date,                         -- Production Date
  purchased_at  date,
  warranty_until date,
  location      text,                           -- free-text location
  location_comment text,
  last_activity_at timestamptz,
  remarks       text,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  created_by    uuid, updated_by uuid,
  version       integer NOT NULL DEFAULT 0,
  deleted_at    timestamptz
);
CREATE INDEX ix_devices_status ON inventory.pos_devices(status) WHERE deleted_at IS NULL;
CREATE INDEX ix_devices_terminal ON inventory.pos_devices(terminal_id);

-- History (SCD-2) for device lifecycle
CREATE TABLE inventory.pos_devices_history (
  history_id  bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id          uuid NOT NULL,
  status      text NOT NULL,
  active_sim_id uuid,
  changed_at  timestamptz NOT NULL DEFAULT now(),
  changed_by  uuid,
  snapshot    jsonb NOT NULL
);

-- =============================================================================
-- DEPLOYMENT (daily deployment + temporal device assignment)
-- =============================================================================
CREATE TABLE deployment.deployments (
  id             uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  deployment_no  text UNIQUE NOT NULL,
  scheduled_date date NOT NULL,
  merchant_id    uuid NOT NULL,                  -- ref merchant.merchants (no cross FK)
  branch_id      uuid NOT NULL,                  -- ref merchant.branches
  assigned_agent uuid,                           -- ref identity.employees (SantimPay employee)
  status         text NOT NULL DEFAULT 'planned'
                 CHECK (status IN ('planned','in_progress','completed','failed','cancelled')),
  received_by    text,                           -- Person at merchant who received the device
  trello_card_id text,                           -- external task ref (Trello integration)
  conversation_notes text,                       -- "Write the Detailed Conversation"
  gps_latitude   numeric(9,6),
  gps_longitude  numeric(9,6),
  completed_at   timestamptz,
  notes          text,
  created_at     timestamptz NOT NULL DEFAULT now(),
  updated_at     timestamptz NOT NULL DEFAULT now(),
  created_by     uuid, updated_by uuid,
  version        integer NOT NULL DEFAULT 0,
  deleted_at     timestamptz
);
CREATE INDEX ix_deploy_date ON deployment.deployments(scheduled_date);
CREATE INDEX ix_deploy_agent ON deployment.deployments(assigned_agent);

CREATE TABLE deployment.device_assignments (
  id          uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  device_id   uuid NOT NULL,                     -- ref inventory.pos_devices
  branch_id   uuid NOT NULL,                     -- ref merchant.branches
  merchant_id uuid NOT NULL,
  deployment_id uuid REFERENCES deployment.deployments(id),
  valid_from  timestamptz NOT NULL DEFAULT now(),
  valid_to    timestamptz,
  is_current  boolean NOT NULL DEFAULT true,
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now(),
  version     integer NOT NULL DEFAULT 0,
  -- A device cannot be assigned to two places at the same time:
  EXCLUDE USING gist (
    device_id WITH =,
    tstzrange(valid_from, valid_to) WITH &&
  )
);
CREATE UNIQUE INDEX ux_assignment_current ON deployment.device_assignments(device_id)
  WHERE is_current;

-- =============================================================================
-- KYC
-- =============================================================================
CREATE TABLE kyc.kyc_requests (
  id           uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  merchant_id  uuid NOT NULL,                    -- ref merchant.merchants
  owner_id     uuid,                             -- ref merchant.merchant_owners
  request_type text NOT NULL DEFAULT 'onboarding'
               CHECK (request_type IN ('onboarding','update','periodic_review')),
  status       text NOT NULL DEFAULT 'draft'
               CHECK (status IN ('draft','submitted','under_review','pending_docs','approved','rejected')),
  reviewer_id  uuid,                             -- ref identity.employees
  decision_reason text,
  submitted_at timestamptz,
  decided_at   timestamptz,
  external_ref text,                             -- ACL ref to gov verification
  created_at   timestamptz NOT NULL DEFAULT now(),
  updated_at   timestamptz NOT NULL DEFAULT now(),
  version      integer NOT NULL DEFAULT 0,
  deleted_at   timestamptz
);
CREATE INDEX ix_kyc_merchant ON kyc.kyc_requests(merchant_id);
CREATE INDEX ix_kyc_status ON kyc.kyc_requests(status);

CREATE TABLE kyc.kyc_history (
  history_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  kyc_id     uuid NOT NULL,
  status     text NOT NULL,
  actor_id   uuid,
  reason     text,
  changed_at timestamptz NOT NULL DEFAULT now(),
  snapshot   jsonb NOT NULL
);

-- =============================================================================
-- WORKFLOW & APPROVALS (generic, drives merchant/deployment/kyc gates)
-- =============================================================================
CREATE TABLE workflow.workflow_instances (
  id            uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  workflow_type text NOT NULL,                   -- 'merchant_activation','device_write_off',...
  subject_type  text NOT NULL,                   -- 'merchant','deployment','kyc'
  subject_id    uuid NOT NULL,
  status        text NOT NULL DEFAULT 'pending'
                CHECK (status IN ('pending','approved','rejected','cancelled')),
  current_step  integer NOT NULL DEFAULT 1,
  total_steps   integer NOT NULL DEFAULT 1,
  initiated_by  uuid,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  version       integer NOT NULL DEFAULT 0
);
CREATE INDEX ix_wf_subject ON workflow.workflow_instances(subject_type, subject_id);

CREATE TABLE workflow.workflow_approvals (
  id          uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  instance_id uuid NOT NULL REFERENCES workflow.workflow_instances(id) ON DELETE CASCADE,
  step_no     integer NOT NULL,
  approver_role text,
  approver_id uuid,
  decision    text CHECK (decision IN ('approved','rejected','delegated')),
  comment     text,
  decided_at  timestamptz,
  created_at  timestamptz NOT NULL DEFAULT now(),
  UNIQUE (instance_id, step_no)
);

-- =============================================================================
-- TASKS & FOLLOW-UPS
-- =============================================================================
CREATE TABLE tasks.tasks (
  id           uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  title        text NOT NULL,
  description  text,
  task_type    text,                             -- 'deployment','kyc_followup','collection',...
  priority     text NOT NULL DEFAULT 'medium' CHECK (priority IN ('low','medium','high','urgent')),
  status       text NOT NULL DEFAULT 'open'
               CHECK (status IN ('open','assigned','in_progress','blocked','done','cancelled')),
  assignee_id  uuid,                             -- ref identity.employees
  related_type text, related_id uuid,            -- polymorphic link
  due_at       timestamptz,
  completed_at timestamptz,
  source       text NOT NULL DEFAULT 'manual' CHECK (source IN ('manual','ai','workflow','system')),
  created_at   timestamptz NOT NULL DEFAULT now(),
  updated_at   timestamptz NOT NULL DEFAULT now(),
  created_by   uuid, updated_by uuid,
  version      integer NOT NULL DEFAULT 0,
  deleted_at   timestamptz
);
CREATE INDEX ix_tasks_assignee_status ON tasks.tasks(assignee_id, status) WHERE deleted_at IS NULL;
CREATE INDEX ix_tasks_due ON tasks.tasks(due_at) WHERE status NOT IN ('done','cancelled');

CREATE TABLE followup.follow_ups (
  id           uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  merchant_id  uuid,                             -- ref merchant.merchants
  task_id      uuid,                             -- ref tasks.tasks
  agent_id     uuid,                             -- ref identity.employees
  channel      text NOT NULL DEFAULT 'call' CHECK (channel IN ('call','sms','email','visit','whatsapp')),
  outcome      text CHECK (outcome IN ('reached','no_answer','callback','resolved','escalated')),
  notes        text,
  ai_generated boolean NOT NULL DEFAULT false,
  contacted_at timestamptz NOT NULL DEFAULT now(),
  next_action_at timestamptz,
  created_at   timestamptz NOT NULL DEFAULT now(),
  updated_at   timestamptz NOT NULL DEFAULT now(),
  version      integer NOT NULL DEFAULT 0,
  deleted_at   timestamptz
);
CREATE INDEX ix_followups_merchant ON followup.follow_ups(merchant_id);

-- =============================================================================
-- ANALYTICS — transaction summaries (partitioned, append-only reference data)
-- =============================================================================
CREATE TABLE analytics.transaction_summary (
  id            uuid NOT NULL DEFAULT shared.uuid_generate_v7(),
  txn_date      date NOT NULL,
  merchant_id   uuid NOT NULL,
  branch_id     uuid,
  device_id     uuid,
  txn_count     integer NOT NULL DEFAULT 0,
  total_amount  numeric(18,2) NOT NULL DEFAULT 0,
  currency      text NOT NULL DEFAULT 'ETB',
  success_count integer NOT NULL DEFAULT 0,
  failed_count  integer NOT NULL DEFAULT 0,
  source_ref    text,                            -- batch/stream provenance
  ingested_at   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id, txn_date)
) PARTITION BY RANGE (txn_date);
CREATE TABLE analytics.transaction_summary_default PARTITION OF analytics.transaction_summary DEFAULT;
CREATE INDEX ix_txn_merchant_date ON analytics.transaction_summary(merchant_id, txn_date);
CREATE INDEX ix_txn_device_date ON analytics.transaction_summary(device_id, txn_date);

-- Monthly transaction + commission rollup (the "Monthly POS Transaction Report")
CREATE TABLE analytics.monthly_transaction_summary (
  id                        uuid NOT NULL DEFAULT shared.uuid_generate_v7(),
  period_month              date NOT NULL,                 -- first day of month
  terminal_id               text NOT NULL,
  terminal_name             text,
  merchant_id               uuid NOT NULL,
  total_purchase_count      bigint NOT NULL DEFAULT 0,
  total_purchase_amount     numeric(18,2) NOT NULL DEFAULT 0,
  gateway_txn_count         bigint NOT NULL DEFAULT 0,
  gateway_txn_amount        numeric(18,2) NOT NULL DEFAULT 0,
  total_txn_count           bigint NOT NULL DEFAULT 0,
  total_txn_amount          numeric(18,2) NOT NULL DEFAULT 0,
  santimpay_commission      numeric(18,2) NOT NULL DEFAULT 0,
  total_commission_br       numeric(18,2) NOT NULL DEFAULT 0,
  total_commission_cut      numeric(18,2) NOT NULL DEFAULT 0,
  currency                  text NOT NULL DEFAULT 'ETB',
  source_ref                text,
  ingested_at               timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id, period_month),
  UNIQUE (period_month, terminal_id)
) PARTITION BY RANGE (period_month);
CREATE TABLE analytics.monthly_transaction_summary_default
  PARTITION OF analytics.monthly_transaction_summary DEFAULT;
CREATE INDEX ix_mtxn_merchant ON analytics.monthly_transaction_summary(merchant_id, period_month);

-- =============================================================================
-- HEALTH — device telemetry (HIGH VOLUME: millions of rows, partitioned monthly)
-- =============================================================================
CREATE TABLE health.device_health_reports (
  id            uuid NOT NULL DEFAULT shared.uuid_generate_v7(),
  reported_at   timestamptz NOT NULL DEFAULT now(),
  device_id     uuid,                            -- ref inventory.pos_devices (by id)
  serial_no     text NOT NULL,                   -- POS SERIAL NUMBER (denormalized for ingest)
  device_status text,                            -- online/offline/...
  battery_level integer CHECK (battery_level BETWEEN 0 AND 100),
  mobile_data   text,                            -- on/off / signal class
  ip_address    inet,
  latitude      numeric(9,6),
  longitude     numeric(9,6),
  cpu_usage     numeric(5,2),
  ram_available_mb integer,
  storage_available_mb integer,
  signal_strength integer,
  app_version   text,
  os_version    text,                            -- Android version
  last_sync_at  timestamptz,
  PRIMARY KEY (id, reported_at)
) PARTITION BY RANGE (reported_at);
CREATE TABLE health.device_health_reports_default
  PARTITION OF health.device_health_reports DEFAULT;
CREATE INDEX ix_health_device_time ON health.device_health_reports(device_id, reported_at DESC);
CREATE INDEX ix_health_serial_time ON health.device_health_reports(serial_no, reported_at DESC);
-- Retention: keep raw 90 days hot, roll up to health.device_health_daily, then detach/archive.

-- Latest-status materialized view (fleet health board)
CREATE MATERIALIZED VIEW health.mv_device_latest_health AS
SELECT DISTINCT ON (device_id)
       device_id, serial_no, device_status, battery_level, signal_strength,
       latitude, longitude, reported_at
FROM health.device_health_reports
WHERE device_id IS NOT NULL
ORDER BY device_id, reported_at DESC
WITH NO DATA;
CREATE UNIQUE INDEX ux_mv_latest_health ON health.mv_device_latest_health(device_id);

-- =============================================================================
-- KYC CHANGE REQUESTS (the "Merchant's KYC Change Request" form)
-- =============================================================================
CREATE TABLE kyc.change_requests (
  id                    uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  merchant_id           uuid NOT NULL,           -- ref merchant.merchants
  terminal_id           text,
  current_trade_name    text,
  owner_full_name       text,
  owner_phone           text,
  change_type           text NOT NULL
                        CHECK (change_type IN ('settlement_account','trade_name','both','other')),
  new_settlement_account text,                    -- 'No' encoded as NULL
  new_trade_name        text,
  reason                text NOT NULL,
  declaration_accepted  boolean NOT NULL DEFAULT false,
  employee_name         text,                     -- SantimPay employee
  merchant_city         text,
  encoder_confirmed     boolean NOT NULL DEFAULT false,
  submitted_email       citext,
  status                text NOT NULL DEFAULT 'submitted'
                        CHECK (status IN ('submitted','under_review','approved','rejected','applied')),
  created_at            timestamptz NOT NULL DEFAULT now(),
  updated_at            timestamptz NOT NULL DEFAULT now(),
  version              integer NOT NULL DEFAULT 0,
  deleted_at           timestamptz,
  CHECK (declaration_accepted) -- declaration is mandatory to submit
);
CREATE INDEX ix_kyc_change_merchant ON kyc.change_requests(merchant_id);

-- =============================================================================
-- NOTIFICATIONS + TRANSACTIONAL OUTBOX (event backbone)
-- =============================================================================
CREATE TABLE notification.notifications (
  id          uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  recipient_id uuid,                             -- ref identity.users
  channel     text NOT NULL CHECK (channel IN ('in_app','email','sms','push')),
  template    text NOT NULL,
  payload     jsonb NOT NULL DEFAULT '{}',
  status      text NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','sent','failed','read')),
  related_type text, related_id uuid,
  sent_at     timestamptz,
  read_at     timestamptz,
  created_at  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_notif_recipient ON notification.notifications(recipient_id, status);

CREATE TABLE notification.outbox (
  id           uuid NOT NULL DEFAULT shared.uuid_generate_v7(),
  occurred_at  timestamptz NOT NULL DEFAULT now(),
  aggregate_type text NOT NULL,
  aggregate_id uuid NOT NULL,
  event_type   text NOT NULL,                    -- e.g. 'MerchantActivated'
  payload      jsonb NOT NULL,
  dispatched   boolean NOT NULL DEFAULT false,
  dispatched_at timestamptz,
  attempts     integer NOT NULL DEFAULT 0,
  PRIMARY KEY (id, occurred_at)
) PARTITION BY RANGE (occurred_at);
CREATE TABLE notification.outbox_default PARTITION OF notification.outbox DEFAULT;
CREATE INDEX ix_outbox_undispatched ON notification.outbox(occurred_at) WHERE NOT dispatched;

-- =============================================================================
-- SHARED — polymorphic attachments (objects live in MinIO; rows are metadata)
-- =============================================================================
CREATE TABLE shared.attachments (
  id           uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  owner_type   text NOT NULL,                    -- 'merchant','kyc','deployment','device'...
  owner_id     uuid NOT NULL,
  file_name    text NOT NULL,
  content_type text NOT NULL,
  size_bytes   bigint NOT NULL,
  storage_key  text NOT NULL,                    -- MinIO object key
  checksum     text,                             -- sha256
  uploaded_by  uuid,
  created_at   timestamptz NOT NULL DEFAULT now(),
  deleted_at   timestamptz
);
CREATE INDEX ix_attachments_owner ON shared.attachments(owner_type, owner_id) WHERE deleted_at IS NULL;

-- =============================================================================
-- AI DATA — scores, features, embeddings (pgvector), conversations
-- =============================================================================
CREATE TABLE ai.scores (
  id          uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  subject_type text NOT NULL,                    -- 'merchant','device'
  subject_id  uuid NOT NULL,
  score_type  text NOT NULL,                     -- 'risk','health','sales','failure_prob'
  value       numeric(6,4) NOT NULL,
  band        text,                              -- 'low'/'medium'/'high'
  model_version text NOT NULL,
  features    jsonb,
  computed_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_scores_subject ON ai.scores(subject_type, subject_id, score_type, computed_at DESC);

CREATE TABLE ai.feature_store (
  subject_type text NOT NULL,
  subject_id   uuid NOT NULL,
  feature_set  text NOT NULL,
  features     jsonb NOT NULL,
  as_of        date NOT NULL,
  PRIMARY KEY (subject_type, subject_id, feature_set, as_of)
);

CREATE TABLE ai.embeddings (
  id          uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  source_type text NOT NULL,                     -- 'doc','followup_note','policy'
  source_id   uuid,
  chunk       text NOT NULL,
  embedding   vector(1024) NOT NULL,             -- dimension depends on model
  metadata    jsonb,
  created_at  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_embeddings_hnsw ON ai.embeddings USING hnsw (embedding vector_cosine_ops);

CREATE TABLE ai.conversations (
  id          uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  user_id     uuid NOT NULL,
  title       text,
  created_at  timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE ai.messages (
  id              uuid PRIMARY KEY DEFAULT shared.uuid_generate_v7(),
  conversation_id uuid NOT NULL REFERENCES ai.conversations(id) ON DELETE CASCADE,
  role            text NOT NULL CHECK (role IN ('user','assistant','system','tool')),
  content         text NOT NULL,
  tokens          integer,
  citations       jsonb,
  created_at      timestamptz NOT NULL DEFAULT now()
);

-- =============================================================================
-- TRIGGERS — attach touch + audit to business tables (sample; repeat per table)
-- =============================================================================
DO $$
DECLARE t text;
BEGIN
  FOR t IN
    SELECT format('%I.%I', schemaname, tablename)
    FROM pg_tables
    WHERE schemaname IN ('identity','merchant','inventory','deployment','kyc',
                         'tasks','workflow','followup')
      AND tablename NOT LIKE '%_history'
  LOOP
    EXECUTE format('CREATE TRIGGER tg_touch BEFORE UPDATE ON %s
                    FOR EACH ROW EXECUTE FUNCTION shared.tg_touch_row();', t);
    EXECUTE format('CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON %s
                    FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();', t);
  END LOOP;
END$$;

-- =============================================================================
-- MATERIALIZED VIEWS (refreshed by analytics worker via pg_cron / on events)
-- =============================================================================
CREATE MATERIALIZED VIEW analytics.mv_device_fleet_status AS
SELECT status, count(*) AS device_count
FROM inventory.pos_devices
WHERE deleted_at IS NULL
GROUP BY status
WITH NO DATA;
CREATE UNIQUE INDEX ux_mv_fleet ON analytics.mv_device_fleet_status(status);

CREATE MATERIALIZED VIEW analytics.mv_daily_deployment_kpi AS
SELECT scheduled_date,
       count(*)                                            AS planned,
       count(*) FILTER (WHERE status='completed')          AS completed,
       count(*) FILTER (WHERE status='failed')             AS failed,
       round(100.0*count(*) FILTER (WHERE status='completed')/NULLIF(count(*),0),2) AS success_pct
FROM deployment.deployments
WHERE deleted_at IS NULL
GROUP BY scheduled_date
WITH NO DATA;
CREATE UNIQUE INDEX ux_mv_deploy_kpi ON analytics.mv_daily_deployment_kpi(scheduled_date);

CREATE MATERIALIZED VIEW analytics.mv_txn_daily_by_branch AS
SELECT txn_date, merchant_id, branch_id,
       sum(txn_count) AS txns, sum(total_amount) AS amount,
       sum(failed_count) AS failures
FROM analytics.transaction_summary
GROUP BY txn_date, merchant_id, branch_id
WITH NO DATA;
CREATE UNIQUE INDEX ux_mv_txn_branch ON analytics.mv_txn_daily_by_branch(txn_date, merchant_id, branch_id);

-- =============================================================================
-- REPORTING VIEWS (live, for BI on read replica)
-- =============================================================================
CREATE VIEW analytics.v_merchant_overview AS
SELECT m.id, m.merchant_no, m.legal_name, m.status, m.risk_tier,
       (SELECT count(*) FROM merchant.branches b WHERE b.merchant_id=m.id AND b.deleted_at IS NULL) AS branches,
       (SELECT count(*) FROM deployment.device_assignments da WHERE da.merchant_id=m.id AND da.is_current) AS active_devices,
       (SELECT value FROM ai.scores s WHERE s.subject_type='merchant' AND s.subject_id=m.id
          AND s.score_type='health' ORDER BY computed_at DESC LIMIT 1) AS health_score
FROM merchant.merchants m
WHERE m.deleted_at IS NULL;

CREATE VIEW analytics.v_employee_productivity AS
SELECT e.id AS employee_id, e.employee_no,
       count(DISTINCT t.id) FILTER (WHERE t.status='done') AS tasks_done,
       count(DISTINCT f.id)                                AS followups_made,
       count(DISTINCT d.id) FILTER (WHERE d.status='completed') AS deployments_completed
FROM identity.employees e
LEFT JOIN tasks.tasks t       ON t.assignee_id=e.id
LEFT JOIN followup.follow_ups f ON f.agent_id=e.id
LEFT JOIN deployment.deployments d ON d.assigned_agent=e.id
GROUP BY e.id, e.employee_no;

-- =============================================================================
-- SEED reference data (permissions catalogue + base roles) — abbreviated
-- =============================================================================
INSERT INTO identity.permissions(resource, action) VALUES
  ('merchant','read'),('merchant','create'),('merchant','update'),('merchant','approve'),
  ('device','read'),('device','create'),('device','assign'),('device','retire'),
  ('deployment','read'),('deployment','create'),('deployment','complete'),
  ('kyc','read'),('kyc','review'),('kyc','approve'),
  ('report','read'),('report','export'),
  ('task','read'),('task','assign'),('user','manage')
ON CONFLICT DO NOTHING;
