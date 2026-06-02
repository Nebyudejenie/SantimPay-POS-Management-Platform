-- ============================================================================
-- V1_0003 inventory: pos_devices + sim_cards. Columns align with
-- com.santimpay.posctl.inventory.domain.{PosDevice,SimCard}.
-- ============================================================================

CREATE TABLE inventory.sim_cards (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  msisdn      text UNIQUE NOT NULL,
  iccid       text UNIQUE,
  carrier     text,
  status      text NOT NULL DEFAULT 'IN_STOCK'
              CHECK (status IN ('IN_STOCK','ACTIVE','SUSPENDED','DEACTIVATED')),
  data_plan   text,
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now(),
  created_by  uuid, updated_by uuid,
  version     integer NOT NULL DEFAULT 0,
  deleted_at  timestamptz
);

CREATE TABLE inventory.pos_devices (
  id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  serial_no        text UNIQUE NOT NULL,
  terminal_id      text UNIQUE,
  imei             text UNIQUE,
  model            text NOT NULL,
  vendor           text,
  qr_code          text,
  kcv              text,
  combined_kcv     text,
  status           text NOT NULL DEFAULT 'IN_STOCK'
                   CHECK (status IN ('IN_STOCK','ALLOCATED','DEPLOYED','FAULTY','IN_REPAIR','RETIRED','LOST')),
  active_sim_id    uuid REFERENCES inventory.sim_cards(id),
  production_date  date,
  warranty_until   date,
  last_activity_at timestamptz,
  created_at       timestamptz NOT NULL DEFAULT now(),
  updated_at       timestamptz NOT NULL DEFAULT now(),
  created_by       uuid, updated_by uuid,
  version          integer NOT NULL DEFAULT 0,
  deleted_at       timestamptz
);
CREATE INDEX ix_devices_status ON inventory.pos_devices(status) WHERE deleted_at IS NULL;
CREATE INDEX ix_devices_terminal ON inventory.pos_devices(terminal_id);

CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON inventory.pos_devices
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON inventory.sim_cards
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();

-- device:update gates DeviceService.markFaulty (and other in-place device edits).
-- The other device:* perms were seeded in V1_0001; this one was missing.
INSERT INTO identity.permissions(resource, action) VALUES ('device','update')
ON CONFLICT DO NOTHING;
INSERT INTO identity.role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r JOIN identity.permissions p
  ON (p.resource, p.action) = ('device','update')
WHERE r.code IN ('SUPER_ADMIN','OPS_MANAGER','FIELD_OFFICER') ON CONFLICT DO NOTHING;
