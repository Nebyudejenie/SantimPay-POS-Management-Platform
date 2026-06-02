-- ============================================================================
-- V1_0004 deployment: deployments + temporal device_assignments.
-- The exclusion constraint guarantees a device is never assigned to two places
-- at the same time (the invariant DeploymentService relies on).
-- ============================================================================

CREATE TABLE deployment.deployments (
  id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  deployment_no      text UNIQUE NOT NULL,
  scheduled_date     date NOT NULL,
  merchant_id        uuid NOT NULL,
  branch_id          uuid NOT NULL,
  device_id          uuid,
  assigned_agent     uuid,
  status             text NOT NULL DEFAULT 'PLANNED'
                     CHECK (status IN ('PLANNED','IN_PROGRESS','COMPLETED','FAILED','CANCELLED')),
  received_by        text,
  trello_card_id     text,
  conversation_notes text,
  gps_latitude       double precision,
  gps_longitude      double precision,
  completed_at       timestamptz,
  created_at         timestamptz NOT NULL DEFAULT now(),
  updated_at         timestamptz NOT NULL DEFAULT now(),
  created_by         uuid, updated_by uuid,
  version            integer NOT NULL DEFAULT 0,
  deleted_at         timestamptz
);
CREATE INDEX ix_deploy_date  ON deployment.deployments(scheduled_date);
CREATE INDEX ix_deploy_agent ON deployment.deployments(assigned_agent);

CREATE TABLE deployment.device_assignments (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  device_id     uuid NOT NULL,
  branch_id     uuid NOT NULL,
  merchant_id   uuid NOT NULL,
  deployment_id uuid REFERENCES deployment.deployments(id),
  valid_from    timestamptz NOT NULL DEFAULT now(),
  valid_to      timestamptz,
  is_current    boolean NOT NULL DEFAULT true,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  created_by    uuid, updated_by uuid,
  version       integer NOT NULL DEFAULT 0,
  -- A device cannot have two overlapping assignment periods:
  CONSTRAINT no_overlapping_assignment EXCLUDE USING gist (
    device_id WITH =,
    tstzrange(valid_from, valid_to) WITH &&
  )
);
-- At most one CURRENT assignment per device:
CREATE UNIQUE INDEX ux_assignment_current ON deployment.device_assignments(device_id)
  WHERE is_current;

CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON deployment.deployments
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON deployment.device_assignments
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();
