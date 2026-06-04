-- V1_0014: Enhance existing tables with missing columns
-- Merchant: company-level details
-- Settlement: payment schedule & history tracking
-- KYC: document storage
-- Device health: proper linking to devices

-- ============================================================================
-- Merchant Table Enhancements
-- ============================================================================
ALTER TABLE merchant.merchants ADD COLUMN IF NOT EXISTS company_phone text;
ALTER TABLE merchant.merchants ADD COLUMN IF NOT EXISTS company_address text;
ALTER TABLE merchant.merchants ADD COLUMN IF NOT EXISTS business_license_number text;
ALTER TABLE merchant.merchants ADD COLUMN IF NOT EXISTS business_license_expiry date;
ALTER TABLE merchant.merchants ADD COLUMN IF NOT EXISTS business_license_verified_at timestamptz;
ALTER TABLE merchant.merchants ADD COLUMN IF NOT EXISTS merchant_tier text DEFAULT 'STANDARD'
  CHECK (merchant_tier IN ('MICRO','STANDARD','PREMIUM'));
ALTER TABLE merchant.merchants ADD COLUMN IF NOT EXISTS annual_revenue numeric(15,2);
ALTER TABLE merchant.merchants ADD COLUMN IF NOT EXISTS number_of_employees integer;
ALTER TABLE merchant.merchants ADD COLUMN IF NOT EXISTS website text;
ALTER TABLE merchant.merchants ADD COLUMN IF NOT EXISTS primary_business_category text;

-- ============================================================================
-- Settlement Accounts Enhancements
-- ============================================================================
ALTER TABLE merchant.settlement_accounts ADD COLUMN IF NOT EXISTS settlement_schedule text DEFAULT 'DAILY'
  CHECK (settlement_schedule IN ('DAILY','WEEKLY','MONTHLY','ON_DEMAND'));
ALTER TABLE merchant.settlement_accounts ADD COLUMN IF NOT EXISTS account_holder_name text;
ALTER TABLE merchant.settlement_accounts ADD COLUMN IF NOT EXISTS account_type text
  CHECK (account_type IN ('SAVINGS','CHECKING','BUSINESS','OTHER'));
ALTER TABLE merchant.settlement_accounts ADD COLUMN IF NOT EXISTS bank_branch_code text;
ALTER TABLE merchant.settlement_accounts ADD COLUMN IF NOT EXISTS verified_by uuid;
ALTER TABLE merchant.settlement_accounts ADD COLUMN IF NOT EXISTS commission_percentage numeric(5,2) DEFAULT 2.5;

-- ============================================================================
-- KYC Requests Enhancements
-- ============================================================================
ALTER TABLE kyc.kyc_requests ADD COLUMN IF NOT EXISTS document_submission_deadline timestamptz;
ALTER TABLE kyc.kyc_requests ADD COLUMN IF NOT EXISTS aml_check_status text DEFAULT 'PENDING'
  CHECK (aml_check_status IN ('PENDING','PASSED','FAILED','FLAGGED'));
ALTER TABLE kyc.kyc_requests ADD COLUMN IF NOT EXISTS identity_verified_at timestamptz;
ALTER TABLE kyc.kyc_requests ADD COLUMN IF NOT EXISTS address_verified_at timestamptz;

-- ============================================================================
-- Device Health Reports — Link to Device (was orphaned!)
-- ============================================================================
ALTER TABLE health.device_health_reports ADD COLUMN IF NOT EXISTS device_id uuid;
ALTER TABLE health.device_health_reports ADD CONSTRAINT fk_health_device
  FOREIGN KEY (device_id) REFERENCES inventory.pos_devices(id) ON DELETE CASCADE;

-- ============================================================================
-- Device Enhancements
-- ============================================================================
ALTER TABLE inventory.pos_devices ADD COLUMN IF NOT EXISTS device_owner text DEFAULT 'SANTIMPAY'
  CHECK (device_owner IN ('SANTIMPAY','MERCHANT'));
ALTER TABLE inventory.pos_devices ADD COLUMN IF NOT EXISTS device_cost numeric(10,2);
ALTER TABLE inventory.pos_devices ADD COLUMN IF NOT EXISTS current_location text;
ALTER TABLE inventory.pos_devices ADD COLUMN IF NOT EXISTS warranty_valid_until date;

-- ============================================================================
-- SIM Card Enhancements
-- ============================================================================
ALTER TABLE inventory.sim_cards ADD COLUMN IF NOT EXISTS sim_owner text DEFAULT 'SANTIMPAY'
  CHECK (sim_owner IN ('SANTIMPAY','CARRIER','MERCHANT'));
ALTER TABLE inventory.sim_cards ADD COLUMN IF NOT EXISTS sim_cost numeric(10,2);
ALTER TABLE inventory.sim_cards ADD COLUMN IF NOT EXISTS monthly_fee numeric(10,2);

-- ============================================================================
-- Deployment Enhancements
-- ============================================================================
ALTER TABLE deployment.deployments ADD COLUMN IF NOT EXISTS merchant_signature bytea;
ALTER TABLE deployment.deployments ADD COLUMN IF NOT EXISTS deployment_photo_url text;
ALTER TABLE deployment.deployments ADD COLUMN IF NOT EXISTS training_completed boolean DEFAULT false;
ALTER TABLE deployment.deployments ADD COLUMN IF NOT EXISTS trainer_name text;
ALTER TABLE deployment.deployments ADD COLUMN IF NOT EXISTS issues_encountered text;
ALTER TABLE deployment.deployments ADD COLUMN IF NOT EXISTS merchant_feedback text;

-- ============================================================================
-- Branch Enhancements
-- ============================================================================
ALTER TABLE merchant.branches ADD COLUMN IF NOT EXISTS operating_hours_start text;
ALTER TABLE merchant.branches ADD COLUMN IF NOT EXISTS operating_hours_end text;
ALTER TABLE merchant.branches ADD COLUMN IF NOT EXISTS is_primary_branch boolean DEFAULT false;
