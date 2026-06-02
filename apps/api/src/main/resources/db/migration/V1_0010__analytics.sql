-- ============================================================================
-- V1_0010 analytics: monthly transaction/commission rollup (partitioned) + report views.
-- Reference reporting data (ADR-006: not a ledger). Columns align with
-- com.santimpay.posctl.analytics.domain.MonthlyTransactionSummary.
-- ============================================================================

CREATE TABLE analytics.monthly_transaction_summary (
  id                    uuid          NOT NULL DEFAULT gen_random_uuid(),
  period_month          date          NOT NULL,
  terminal_id           text          NOT NULL,
  terminal_name         text,
  merchant_id           uuid          NOT NULL,
  total_purchase_count  bigint        NOT NULL DEFAULT 0,
  total_purchase_amount numeric(18,2) NOT NULL DEFAULT 0,
  gateway_txn_count     bigint        NOT NULL DEFAULT 0,
  gateway_txn_amount    numeric(18,2) NOT NULL DEFAULT 0,
  total_txn_count       bigint        NOT NULL DEFAULT 0,
  total_txn_amount      numeric(18,2) NOT NULL DEFAULT 0,
  santimpay_commission  numeric(18,2) NOT NULL DEFAULT 0,
  total_commission_br   numeric(18,2) NOT NULL DEFAULT 0,
  total_commission_cut  numeric(18,2) NOT NULL DEFAULT 0,
  currency              text          NOT NULL DEFAULT 'ETB',
  source_ref            text,
  ingested_at           timestamptz   NOT NULL DEFAULT now(),
  PRIMARY KEY (id, period_month),
  UNIQUE (period_month, terminal_id)
) PARTITION BY RANGE (period_month);

CREATE TABLE analytics.monthly_transaction_summary_default
  PARTITION OF analytics.monthly_transaction_summary DEFAULT;
CREATE INDEX ix_mtxn_merchant ON analytics.monthly_transaction_summary(merchant_id, period_month);

-- Live reporting view: merchant overview for BI / dashboards (read replica).
CREATE VIEW analytics.v_merchant_overview AS
SELECT m.id, m.merchant_no, m.legal_name, m.status, m.risk_tier,
       (SELECT count(*) FROM merchant.branches b
         WHERE b.merchant_id = m.id AND b.deleted_at IS NULL) AS branches,
       (SELECT count(*) FROM deployment.device_assignments da
         WHERE da.merchant_id = m.id AND da.is_current) AS active_devices
FROM merchant.merchants m
WHERE m.deleted_at IS NULL;
