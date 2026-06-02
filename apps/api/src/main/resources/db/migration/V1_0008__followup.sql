-- ============================================================================
-- V1_0008 followup: follow_ups + permissions. Columns align with
-- com.santimpay.posctl.followup.domain.FollowUp.
-- ============================================================================

CREATE TABLE followup.follow_ups (
  id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id      uuid,
  task_id          uuid,
  agent_id         uuid,
  channel          text NOT NULL CHECK (channel IN ('CALL','SMS','EMAIL','VISIT','WHATSAPP')),
  outcome          text CHECK (outcome IN ('REACHED','NO_ANSWER','CALLBACK','RESOLVED','ESCALATED')),
  notes            text,
  contacted_person text,
  contacted_phone  text,
  ai_generated     boolean NOT NULL DEFAULT false,
  contacted_at     timestamptz NOT NULL DEFAULT now(),
  next_action_at   timestamptz,
  created_at       timestamptz NOT NULL DEFAULT now(),
  updated_at       timestamptz NOT NULL DEFAULT now(),
  created_by       uuid, updated_by uuid,
  version          integer NOT NULL DEFAULT 0,
  deleted_at       timestamptz
);
CREATE INDEX ix_followups_merchant ON followup.follow_ups(merchant_id);
CREATE INDEX ix_followups_agent ON followup.follow_ups(agent_id);
CREATE INDEX ix_followups_next_action ON followup.follow_ups(next_action_at)
  WHERE next_action_at IS NOT NULL;

CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON followup.follow_ups
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();

-- Permissions + grants.
INSERT INTO identity.permissions(resource, action) VALUES
  ('followup','read'), ('followup','create')
ON CONFLICT DO NOTHING;

INSERT INTO identity.role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r JOIN identity.permissions p
  ON (p.resource, p.action) IN (('followup','read'),('followup','create'))
WHERE r.code IN ('SUPER_ADMIN','OPS_MANAGER','CALL_CENTER') ON CONFLICT DO NOTHING;
