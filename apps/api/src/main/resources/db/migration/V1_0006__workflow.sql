-- ============================================================================
-- V1_0006 workflow: generic approval engine + new permissions.
-- ============================================================================

CREATE TABLE workflow.workflow_instances (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  workflow_type text NOT NULL,
  subject_type  text NOT NULL,
  subject_id    uuid NOT NULL,
  status        text NOT NULL DEFAULT 'PENDING'
                CHECK (status IN ('PENDING','APPROVED','REJECTED','CANCELLED')),
  current_step  integer NOT NULL DEFAULT 1,
  total_steps   integer NOT NULL DEFAULT 1,
  initiated_by  uuid,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  created_by    uuid, updated_by uuid,
  version       integer NOT NULL DEFAULT 0,
  deleted_at    timestamptz
);
CREATE INDEX ix_wf_subject ON workflow.workflow_instances(subject_type, subject_id);
CREATE INDEX ix_wf_status  ON workflow.workflow_instances(status);

CREATE TABLE workflow.workflow_approvals (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  instance_id uuid NOT NULL REFERENCES workflow.workflow_instances(id) ON DELETE CASCADE,
  step_no     integer NOT NULL,
  approver_id uuid,
  decision    text NOT NULL CHECK (decision IN ('approved','rejected','delegated')),
  comment     text,
  decided_at  timestamptz NOT NULL DEFAULT now(),
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now(),
  created_by  uuid, updated_by uuid,
  version     integer NOT NULL DEFAULT 0
);
CREATE INDEX ix_wf_appr_instance ON workflow.workflow_approvals(instance_id);

CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON workflow.workflow_instances
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON workflow.workflow_approvals
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();

-- New permissions + grants to operational roles.
INSERT INTO identity.permissions(resource, action) VALUES
  ('workflow','read'), ('workflow','approve')
ON CONFLICT DO NOTHING;

INSERT INTO identity.role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r JOIN identity.permissions p
  ON (p.resource, p.action) IN (('workflow','read'),('workflow','approve'))
WHERE r.code IN ('SUPER_ADMIN','OPS_MANAGER') ON CONFLICT DO NOTHING;

INSERT INTO identity.role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r JOIN identity.permissions p
  ON (p.resource, p.action) = ('workflow','read')
WHERE r.code = 'CALL_CENTER' ON CONFLICT DO NOTHING;
