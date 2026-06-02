-- ============================================================================
-- V1_0000 baseline: extensions, schemas, shared functions, audit + outbox spine.
-- Full reference DDL lives in /db/schema.sql; migrations are the executable truth.
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS btree_gist;
-- 'vector' (pgvector) is enabled in the ai migration; image already includes it.

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

-- ---- audit log (partitioned, immutable) -------------------------------------
CREATE TABLE audit.audit_log (
  id           uuid        NOT NULL DEFAULT gen_random_uuid(),
  occurred_at  timestamptz NOT NULL DEFAULT now(),
  schema_name  text        NOT NULL,
  table_name   text        NOT NULL,
  row_id       uuid,
  action       text        NOT NULL CHECK (action IN ('INSERT','UPDATE','DELETE')),
  actor_id     uuid,
  old_data     jsonb,
  new_data     jsonb,
  diff         jsonb,
  request_id   text,
  PRIMARY KEY (id, occurred_at)
) PARTITION BY RANGE (occurred_at);
CREATE TABLE audit.audit_log_default PARTITION OF audit.audit_log DEFAULT;
CREATE INDEX ix_audit_table_row ON audit.audit_log (schema_name, table_name, row_id);
CREATE INDEX ix_audit_actor     ON audit.audit_log (actor_id);

CREATE OR REPLACE FUNCTION shared.tg_audit() RETURNS trigger AS $$
DECLARE
  v_old jsonb := CASE WHEN TG_OP <> 'INSERT' THEN to_jsonb(OLD) END;
  v_new jsonb := CASE WHEN TG_OP <> 'DELETE' THEN to_jsonb(NEW) END;
BEGIN
  INSERT INTO audit.audit_log(schema_name, table_name, row_id, action, actor_id,
                              old_data, new_data, diff, request_id)
  VALUES (TG_TABLE_SCHEMA, TG_TABLE_NAME,
          COALESCE((v_new->>'id')::uuid, (v_old->>'id')::uuid),
          TG_OP,
          NULLIF(current_setting('app.actor_id', true),'')::uuid,
          v_old, v_new,
          CASE WHEN TG_OP='UPDATE' THEN (
            SELECT jsonb_object_agg(key, value) FROM jsonb_each(v_new)
            WHERE v_new->key IS DISTINCT FROM v_old->key) END,
          NULLIF(current_setting('app.request_id', true),''));
  RETURN COALESCE(NEW, OLD);
END;$$ LANGUAGE plpgsql;

-- ---- transactional outbox (partitioned) -------------------------------------
CREATE TABLE notification.outbox (
  id             uuid        NOT NULL DEFAULT gen_random_uuid(),
  occurred_at    timestamptz NOT NULL DEFAULT now(),
  aggregate_type text        NOT NULL,
  aggregate_id   uuid        NOT NULL,
  event_type     text        NOT NULL,
  payload        jsonb       NOT NULL,
  dispatched     boolean     NOT NULL DEFAULT false,
  dispatched_at  timestamptz,
  attempts       integer     NOT NULL DEFAULT 0,
  PRIMARY KEY (id, occurred_at)
) PARTITION BY RANGE (occurred_at);
CREATE TABLE notification.outbox_default PARTITION OF notification.outbox DEFAULT;
CREATE INDEX ix_outbox_undispatched ON notification.outbox(occurred_at) WHERE NOT dispatched;

-- ---- Spring Modulith JDBC event publication registry --------------------------
-- Tracks delivery of @ApplicationModuleListener events; incomplete ones are re-published
-- on restart. Flyway owns this DDL (schema-initialization is disabled in application.yml).
CREATE TABLE event_publication (
  id               uuid         NOT NULL,
  listener_id      text         NOT NULL,
  event_type       text         NOT NULL,
  serialized_event text         NOT NULL,
  publication_date timestamptz  NOT NULL,
  completion_date  timestamptz,
  PRIMARY KEY (id)
);
CREATE INDEX ix_event_publication_incomplete
  ON event_publication (completion_date) WHERE completion_date IS NULL;
CREATE INDEX ix_event_publication_by_listener_and_date
  ON event_publication (listener_id, serialized_event);
