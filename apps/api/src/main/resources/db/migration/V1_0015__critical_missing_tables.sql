-- V1_0015: Create critical missing tables for production POS system
-- Attachments, deployment events, settlement history, merchant documents, compliance tracking

-- ============================================================================
-- shared.attachments — Store all documents, photos, signatures
-- ============================================================================
CREATE TABLE shared.attachments (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  entity_type   text NOT NULL,  -- merchant, kyc, deployment, device, branch, settlement
  entity_id     uuid NOT NULL,
  document_type text NOT NULL,  -- license, id_photo, business_reg, contract, deployment_photo, signature, merchant_photo
  file_name     text NOT NULL,
  file_size     integer,
  file_mime_type text,
  file_path     text NOT NULL,  -- s3://bucket/path or minio path
  description   text,
  uploaded_by   uuid,
  verified_by   uuid,
  verified_at   timestamptz,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  created_by    uuid, updated_by uuid,
  version       integer NOT NULL DEFAULT 0,
  deleted_at    timestamptz
);
CREATE INDEX ix_attachments_entity ON shared.attachments(entity_type, entity_id) WHERE deleted_at IS NULL;
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON shared.attachments
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();

-- ============================================================================
-- deployment.deployment_events — Event timeline for each deployment
-- ============================================================================
CREATE TABLE deployment.deployment_events (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  deployment_id   uuid NOT NULL REFERENCES deployment.deployments(id) ON DELETE CASCADE,
  event_type      text NOT NULL,  -- assigned, agent_departed, arrived, demo_started, setup_complete, merchant_trained, signature_collected, photo_uploaded, completed, failed
  event_status    text NOT NULL DEFAULT 'COMPLETED' CHECK (event_status IN ('PENDING','IN_PROGRESS','COMPLETED','FAILED')),
  event_timestamp timestamptz NOT NULL DEFAULT now(),
  agent_id        uuid,
  description     text,
  photo_url       text,
  gps_latitude    double precision,
  gps_longitude   double precision,
  remarks         text,
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now(),
  created_by      uuid, updated_by uuid,
  version         integer NOT NULL DEFAULT 0,
  deleted_at      timestamptz
);
CREATE INDEX ix_deployment_events_deployment ON deployment.deployment_events(deployment_id);
CREATE INDEX ix_deployment_events_timestamp ON deployment.deployment_events(event_timestamp DESC);
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON deployment.deployment_events
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();

-- ============================================================================
-- merchant.settlement_history — Track what merchants earn
-- ============================================================================
CREATE TABLE merchant.settlement_history (
  id                     uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id            uuid NOT NULL REFERENCES merchant.merchants(id) ON DELETE CASCADE,
  settlement_account_id  uuid REFERENCES merchant.settlement_accounts(id),
  settlement_period_from date NOT NULL,
  settlement_period_to   date NOT NULL,
  gross_amount           numeric(15,2) NOT NULL,
  commission_amount      numeric(15,2) NOT NULL,
  net_amount             numeric(15,2) NOT NULL,
  transaction_count      integer,
  settlement_status      text NOT NULL DEFAULT 'PENDING'
                         CHECK (settlement_status IN ('PENDING','IN_PROGRESS','COMPLETED','FAILED','REVERSED')),
  transferred_at         timestamptz,
  transferred_by         uuid,
  bank_reference         text,
  notes                  text,
  created_at             timestamptz NOT NULL DEFAULT now(),
  updated_at             timestamptz NOT NULL DEFAULT now(),
  created_by             uuid, updated_by uuid,
  version                integer NOT NULL DEFAULT 0,
  deleted_at             timestamptz
);
CREATE INDEX ix_settlement_merchant ON merchant.settlement_history(merchant_id);
CREATE INDEX ix_settlement_period ON merchant.settlement_history(settlement_period_from, settlement_period_to);
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON merchant.settlement_history
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();

-- ============================================================================
-- merchant.merchant_documents — Track licenses, registrations, certifications
-- ============================================================================
CREATE TABLE merchant.merchant_documents (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id     uuid NOT NULL REFERENCES merchant.merchants(id) ON DELETE CASCADE,
  document_type   text NOT NULL,  -- business_license, tax_certificate, bank_statement, proof_of_address, id_copy
  document_number text,
  issue_date      date,
  expiry_date     date,
  issuing_authority text,
  file_path       text,
  verified_by     uuid,
  verified_at     timestamptz,
  verification_notes text,
  is_current      boolean DEFAULT true,
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now(),
  created_by      uuid, updated_by uuid,
  version         integer NOT NULL DEFAULT 0,
  deleted_at      timestamptz
);
CREATE INDEX ix_merchant_docs_merchant ON merchant.merchant_documents(merchant_id);
CREATE INDEX ix_merchant_docs_expiry ON merchant.merchant_documents(expiry_date) WHERE deleted_at IS NULL;
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON merchant.merchant_documents
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();

-- ============================================================================
-- merchant.compliance_checklist — Audit trail for KYC, AML, regulatory checks
-- ============================================================================
CREATE TABLE merchant.compliance_checklist (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id     uuid NOT NULL REFERENCES merchant.merchants(id) ON DELETE CASCADE,
  check_type      text NOT NULL,  -- kyc, aml, identity_verified, address_verified, bank_verified, license_verified, pep_check, sanctions_check
  check_status    text NOT NULL DEFAULT 'PENDING'
                  CHECK (check_status IN ('PENDING','IN_PROGRESS','PASSED','FAILED','EXPIRED','PENDING_REVIEW')),
  checked_by      uuid,
  checked_at      timestamptz,
  expiry_date     date,  -- when to re-check?
  findings        text,  -- any issues found?
  evidence_link   text,  -- attachment ID or URL
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now(),
  created_by      uuid, updated_by uuid,
  version         integer NOT NULL DEFAULT 0,
  deleted_at      timestamptz
);
CREATE INDEX ix_compliance_merchant ON merchant.compliance_checklist(merchant_id);
CREATE INDEX ix_compliance_status ON merchant.compliance_checklist(check_status) WHERE deleted_at IS NULL;
CREATE INDEX ix_compliance_expiry ON merchant.compliance_checklist(expiry_date) WHERE check_status = 'PASSED';
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON merchant.compliance_checklist
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();

-- ============================================================================
-- merchant.merchant_stakeholders — Track all key contacts (not just owners)
-- ============================================================================
CREATE TABLE merchant.merchant_stakeholders (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id   uuid NOT NULL REFERENCES merchant.merchants(id) ON DELETE CASCADE,
  full_name     text NOT NULL,
  role          text NOT NULL,  -- owner, operator, financial_signatory, technical_contact, manager, accountant
  national_id   text,
  phone         text,
  email         citext,
  is_primary    boolean DEFAULT false,
  active_from   date DEFAULT CURRENT_DATE,
  active_until  date,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  created_by    uuid, updated_by uuid,
  version       integer NOT NULL DEFAULT 0,
  deleted_at    timestamptz
);
CREATE INDEX ix_stakeholders_merchant ON merchant.merchant_stakeholders(merchant_id);
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON merchant.merchant_stakeholders
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();

-- ============================================================================
-- inventory.device_maintenance — Track device repairs, replacements, upgrades
-- ============================================================================
CREATE TABLE inventory.device_maintenance (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  device_id       uuid NOT NULL REFERENCES inventory.pos_devices(id) ON DELETE CASCADE,
  maintenance_type text NOT NULL,  -- repair, replacement, upgrade, inspection, software_update, firmware_update
  issue_description text,
  resolution       text,
  parts_replaced   text,
  technician_id    uuid,
  start_date       timestamptz NOT NULL DEFAULT now(),
  completion_date  timestamptz,
  cost             numeric(10,2),
  vendor_name      text,
  warranty_claim   boolean DEFAULT false,
  created_at       timestamptz NOT NULL DEFAULT now(),
  updated_at       timestamptz NOT NULL DEFAULT now(),
  created_by       uuid, updated_by uuid,
  version          integer NOT NULL DEFAULT 0,
  deleted_at       timestamptz
);
CREATE INDEX ix_maintenance_device ON inventory.device_maintenance(device_id);
CREATE INDEX ix_maintenance_date ON inventory.device_maintenance(start_date DESC);
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON inventory.device_maintenance
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();

-- ============================================================================
-- inventory.sim_allocation_history — Track which SIM was in which device when
-- ============================================================================
CREATE TABLE inventory.sim_allocation_history (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  device_id     uuid NOT NULL REFERENCES inventory.pos_devices(id) ON DELETE CASCADE,
  sim_id        uuid NOT NULL REFERENCES inventory.sim_cards(id) ON DELETE RESTRICT,
  allocated_at  timestamptz NOT NULL DEFAULT now(),
  deallocated_at timestamptz,
  reason        text,  -- initial_setup, sim_replacement, sim_upgrade, troubleshooting
  allocated_by  uuid,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  created_by    uuid, updated_by uuid,
  version       integer NOT NULL DEFAULT 0,
  deleted_at    timestamptz
);
CREATE INDEX ix_sim_history_device ON inventory.sim_allocation_history(device_id);
CREATE INDEX ix_sim_history_sim ON inventory.sim_allocation_history(sim_id);
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON inventory.sim_allocation_history
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();

-- ============================================================================
-- shared.remarks — General notes/annotations on any entity
-- ============================================================================
CREATE TABLE shared.remarks (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  entity_type text NOT NULL,  -- merchant, device, deployment, kyc, branch, settlement
  entity_id   uuid NOT NULL,
  remark_type text,  -- issue, note, followup, risk, escalation
  content     text NOT NULL,
  priority    text DEFAULT 'NORMAL' CHECK (priority IN ('LOW','NORMAL','HIGH','URGENT')),
  created_by  uuid,
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now(),
  deleted_at  timestamptz
);
CREATE INDEX ix_remarks_entity ON shared.remarks(entity_type, entity_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_remarks_type ON shared.remarks(remark_type);
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON shared.remarks
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();

-- ============================================================================
-- Permissions for new operations
-- ============================================================================
INSERT INTO identity.permissions(resource, action) VALUES
  ('attachment','upload'), ('attachment','download'), ('attachment','verify'),
  ('merchant','documents'), ('merchant','compliance'),
  ('settlement','view_history'),
  ('device','maintenance'),
  ('deployment','events')
ON CONFLICT DO NOTHING;

-- Grant to roles
INSERT INTO identity.role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r JOIN identity.permissions p
  ON (p.resource, p.action) IN (('attachment','upload'),('attachment','download'),('merchant','documents'),('deployment','events'))
WHERE r.code IN ('SUPER_ADMIN','OPS_MANAGER','COMPLIANCE','DATA_ENCODER') ON CONFLICT DO NOTHING;

INSERT INTO identity.role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r JOIN identity.permissions p
  ON (p.resource, p.action) IN (('settlement','view_history'),('merchant','documents'))
WHERE r.code = 'FINANCE' ON CONFLICT DO NOTHING;
