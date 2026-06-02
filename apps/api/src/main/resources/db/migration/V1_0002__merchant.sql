-- ============================================================================
-- V1_0002 merchant: merchants + owners + branches + settlement accounts.
-- Columns align with com.santimpay.posctl.merchant.domain.Merchant.
-- ============================================================================

CREATE TABLE merchant.merchants (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_no  text UNIQUE NOT NULL,
  legal_name   text NOT NULL,
  trade_name   text,
  tax_id       text,
  category     text,
  status       text NOT NULL DEFAULT 'ONBOARDING'
               CHECK (status IN ('ONBOARDING','PENDING_KYC','ACTIVE','SUSPENDED','CLOSED')),
  risk_tier    text CHECK (risk_tier IN ('LOW','MEDIUM','HIGH')),
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
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id   uuid NOT NULL REFERENCES merchant.merchants(id) ON DELETE CASCADE,
  full_name     text NOT NULL,
  national_id   text,
  phone         text,
  email         citext,
  ownership_pct numeric(5,2) CHECK (ownership_pct BETWEEN 0 AND 100),
  is_primary    boolean NOT NULL DEFAULT false,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  version       integer NOT NULL DEFAULT 0,
  deleted_at    timestamptz
);
CREATE INDEX ix_owners_merchant ON merchant.merchant_owners(merchant_id);

CREATE TABLE merchant.branches (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id   uuid NOT NULL REFERENCES merchant.merchants(id) ON DELETE CASCADE,
  branch_no     text NOT NULL,
  name          text NOT NULL,
  region        text, city text, sub_city text, woreda text,
  address_line  text,
  latitude      numeric(9,6), longitude numeric(9,6),
  contact_phone text,
  status        text NOT NULL DEFAULT 'active' CHECK (status IN ('active','inactive','closed')),
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  version       integer NOT NULL DEFAULT 0,
  deleted_at    timestamptz,
  UNIQUE (merchant_id, branch_no)
);
CREATE INDEX ix_branches_merchant ON merchant.branches(merchant_id) WHERE deleted_at IS NULL;

CREATE TABLE inventory.banks (
  id        uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  code      text UNIQUE NOT NULL,
  name      text NOT NULL,
  swift     text,
  is_active boolean NOT NULL DEFAULT true
);

CREATE TABLE merchant.settlement_accounts (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
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

-- Attach the audit trigger to merchant tables.
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON merchant.merchants
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON merchant.branches
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();
CREATE TRIGGER tg_audit AFTER INSERT OR UPDATE OR DELETE ON merchant.merchant_owners
  FOR EACH ROW EXECUTE FUNCTION shared.tg_audit();
