-- ============================================================================
-- V1_0007 tasks: tasks + permissions. Columns align with
-- com.santimpay.posctl.tasks.domain.Task.
-- ============================================================================

CREATE TABLE tasks.tasks (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  title        text NOT NULL,
  description  text,
  task_type    text,
  priority     text NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW','MEDIUM','HIGH','URGENT')),
  status       text NOT NULL DEFAULT 'OPEN'
               CHECK (status IN ('OPEN','ASSIGNED','IN_PROGRESS','BLOCKED','DONE','CANCELLED')),
  assignee_id  uuid,
  related_type text,
  related_id   uuid,
  due_at       timestamptz,
  completed_at timestamptz,
  source       text NOT NULL DEFAULT 'MANUAL' CHECK (source IN ('MANUAL','AI','WORKFLOW','SYSTEM')),
  created_at   timestamptz NOT NULL DEFAULT now(),
  updated_at   timestamptz NOT NULL DEFAULT now(),
  created_by   uuid, updated_by uuid,
  version      integer NOT NULL DEFAULT 0,
  deleted_at   timestamptz
);
CREATE INDEX ix_tasks_assignee_status ON tasks.tasks(assignee_id, status) WHERE deleted_at IS NULL;
CREATE INDEX ix_tasks_due ON tasks.tasks(due_at) WHERE status NOT IN ('DONE','CANCELLED');
CREATE INDEX ix_tasks_related ON tasks.tasks(related_type, related_id);

CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON tasks.tasks
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();

-- Permissions + grants.
INSERT INTO identity.permissions(resource, action) VALUES
  ('task','create'), ('task','update')
ON CONFLICT DO NOTHING;
-- ('task','read') and ('task','assign') already seeded in V1_0001.

INSERT INTO identity.role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r JOIN identity.permissions p
  ON (p.resource, p.action) IN (('task','create'),('task','update'),('task','assign'),('task','read'))
WHERE r.code IN ('SUPER_ADMIN','OPS_MANAGER') ON CONFLICT DO NOTHING;

INSERT INTO identity.role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r JOIN identity.permissions p
  ON (p.resource, p.action) IN (('task','read'),('task','update'))
WHERE r.code IN ('CALL_CENTER','FIELD_OFFICER') ON CONFLICT DO NOTHING;
