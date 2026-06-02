-- ============================================================================
-- V1_0005 kyc: kyc_requests (+ history) + change_requests.
-- Columns align with com.santimpay.posctl.kyc.domain.{KycRequest,KycChangeRequest}.
-- ============================================================================

CREATE TABLE kyc.kyc_requests (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id     uuid NOT NULL,
  owner_id        uuid,
  request_type    text NOT NULL DEFAULT 'onboarding'
                  CHECK (request_type IN ('onboarding','update','periodic_review')),
  status          text NOT NULL DEFAULT 'DRAFT'
                  CHECK (status IN ('DRAFT','SUBMITTED','UNDER_REVIEW','PENDING_DOCS','APPROVED','REJECTED')),
  reviewer_id     uuid,
  decision_reason text,
  submitted_at    timestamptz,
  decided_at      timestamptz,
  external_ref    text,
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now(),
  created_by      uuid, updated_by uuid,
  version         integer NOT NULL DEFAULT 0,
  deleted_at      timestamptz
);
CREATE INDEX ix_kyc_merchant ON kyc.kyc_requests(merchant_id);
CREATE INDEX ix_kyc_status   ON kyc.kyc_requests(status);

CREATE TABLE kyc.change_requests (
  id                     uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id            uuid NOT NULL,
  terminal_id            text,
  current_trade_name     text,
  owner_full_name        text,
  owner_phone            text,
  change_type            text NOT NULL
                         CHECK (change_type IN ('settlement_account','trade_name','both','other')),
  new_settlement_account text,
  new_trade_name         text,
  reason                 text NOT NULL,
  declaration_accepted   boolean NOT NULL DEFAULT false,
  employee_name          text,
  merchant_city          text,
  encoder_confirmed      boolean NOT NULL DEFAULT false,
  status                 text NOT NULL DEFAULT 'submitted'
                         CHECK (status IN ('submitted','under_review','approved','rejected','applied')),
  created_at             timestamptz NOT NULL DEFAULT now(),
  updated_at             timestamptz NOT NULL DEFAULT now(),
  created_by             uuid, updated_by uuid,
  version                integer NOT NULL DEFAULT 0,
  deleted_at             timestamptz,
  CHECK (declaration_accepted)
);
CREATE INDEX ix_kyc_change_merchant ON kyc.change_requests(merchant_id);

CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON kyc.kyc_requests
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON kyc.change_requests
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();
