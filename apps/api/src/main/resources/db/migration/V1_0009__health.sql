-- ============================================================================
-- V1_0009 health: device telemetry (HIGH VOLUME, monthly range-partitioned).
-- No audit trigger / soft-delete: append-only facts. Columns align with
-- com.santimpay.posctl.health.domain.DeviceHealthReport.
-- ============================================================================

CREATE TABLE health.device_health_reports (
  id                   uuid        NOT NULL DEFAULT gen_random_uuid(),
  reported_at          timestamptz NOT NULL DEFAULT now(),
  device_id            uuid,
  serial_no            text        NOT NULL,
  device_status        text,
  battery_level        integer CHECK (battery_level BETWEEN 0 AND 100),
  mobile_data          text,
  ip_address           text,
  latitude             double precision,
  longitude            double precision,
  cpu_usage            double precision,
  ram_available_mb     integer,
  storage_available_mb integer,
  signal_strength      integer,
  app_version          text,
  os_version           text,
  last_sync_at         timestamptz,
  PRIMARY KEY (id, reported_at)
) PARTITION BY RANGE (reported_at);

CREATE TABLE health.device_health_reports_default
  PARTITION OF health.device_health_reports DEFAULT;

CREATE INDEX ix_health_device_time ON health.device_health_reports(device_id, reported_at DESC);
CREATE INDEX ix_health_serial_time ON health.device_health_reports(serial_no, reported_at DESC);

-- Retention/partition rollover is automated by pg_partman in production (90-day hot window):
--   SELECT partman.create_parent('health.device_health_reports','reported_at','native','monthly');
-- plus a daily rollup into health.device_health_daily before detaching old partitions.

-- Latest-status materialized view for the fleet board (refreshed by the analytics worker).
CREATE MATERIALIZED VIEW health.mv_device_latest_health AS
SELECT DISTINCT ON (device_id)
       device_id, serial_no, device_status, battery_level, signal_strength,
       latitude, longitude, reported_at
FROM health.device_health_reports
WHERE device_id IS NOT NULL
ORDER BY device_id, reported_at DESC
WITH NO DATA;
CREATE UNIQUE INDEX ux_mv_latest_health ON health.mv_device_latest_health(device_id);

-- device:telemetry already seeded in V1_0001 and granted to FIELD_OFFICER; grant to ops too.
INSERT INTO identity.role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r JOIN identity.permissions p
  ON (p.resource, p.action) = ('device','telemetry')
WHERE r.code IN ('SUPER_ADMIN','OPS_MANAGER') ON CONFLICT DO NOTHING;
