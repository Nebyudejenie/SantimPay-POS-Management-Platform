-- Forward migration: merchant_owners and settlement_accounts now extend AggregateRoot, which maps
-- created_by/updated_by (embedded AuditMetadata). The original V1_0002 predates that change, so add
-- the columns here idempotently. Safe on both fresh installs and the already-migrated live DB.
ALTER TABLE merchant.merchant_owners     ADD COLUMN IF NOT EXISTS created_by uuid;
ALTER TABLE merchant.merchant_owners     ADD COLUMN IF NOT EXISTS updated_by uuid;
ALTER TABLE merchant.settlement_accounts ADD COLUMN IF NOT EXISTS created_by uuid;
ALTER TABLE merchant.settlement_accounts ADD COLUMN IF NOT EXISTS updated_by uuid;
