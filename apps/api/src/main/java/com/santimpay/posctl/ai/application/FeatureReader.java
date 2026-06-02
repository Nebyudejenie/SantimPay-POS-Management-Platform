package com.santimpay.posctl.ai.application;

import com.santimpay.posctl.ai.domain.MerchantFeatures;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Assembles {@link MerchantFeatures} from operational data for the offline scorers. Like
 * {@code analytics.AnalyticsReadModel}, this is the sanctioned read-only cross-schema path for the
 * AI module — all such reads are confined here (kept out of the scorers, which stay pure). In
 * production these queries run against the read replica.
 */
@Component
@RequiredArgsConstructor
public class FeatureReader {

    private final JdbcTemplate jdbc;

    /** All non-deleted merchant ids (the scoring population). */
    public List<UUID> merchantIdsToScore() {
        return jdbc.queryForList(
                "select id from merchant.merchants where deleted_at is null", UUID.class);
    }

    public MerchantFeatures featuresFor(UUID merchantId) {
        boolean active = Boolean.TRUE.equals(jdbc.queryForObject(
                "select status = 'ACTIVE' from merchant.merchants where id = ?",
                Boolean.class, merchantId));

        boolean kycApproved = count(
                "select count(*) from kyc.kyc_requests where merchant_id = ? and status = 'APPROVED'",
                merchantId) > 0;

        int branchCount = (int) count(
                "select count(*) from merchant.branches where merchant_id = ? and deleted_at is null",
                merchantId);

        int activeDevices = (int) count("""
                select count(*) from deployment.device_assignments
                where merchant_id = ? and is_current = true""", merchantId);

        // Faulty devices currently assigned to this merchant (join assignment -> device status).
        int faultyDevices = (int) count("""
                select count(*) from deployment.device_assignments da
                join inventory.pos_devices d on d.id = da.device_id
                where da.merchant_id = ? and da.is_current = true and d.status = 'FAULTY'""", merchantId);

        long txn30 = count("""
                select coalesce(sum(txn_count),0) from analytics.transaction_summary
                where merchant_id = ? and txn_date >= current_date - interval '30 days'""", merchantId);
        long txnPrev30 = count("""
                select coalesce(sum(txn_count),0) from analytics.transaction_summary
                where merchant_id = ? and txn_date >= current_date - interval '60 days'
                  and txn_date < current_date - interval '30 days'""", merchantId);

        BigDecimal amount30 = jdbc.queryForObject("""
                select coalesce(sum(total_amount),0) from analytics.transaction_summary
                where merchant_id = ? and txn_date >= current_date - interval '30 days'""",
                BigDecimal.class, merchantId);

        int unresolvedFollowUps = (int) count("""
                select count(*) from followup.follow_ups
                where merchant_id = ? and outcome in ('NO_ANSWER','CALLBACK','ESCALATED')
                  and deleted_at is null""", merchantId);

        Integer daysSince = jdbc.queryForObject("""
                select coalesce(extract(day from now() - max(txn_date))::int, 999)
                from analytics.transaction_summary where merchant_id = ?""",
                Integer.class, merchantId);

        return new MerchantFeatures(merchantId, active, kycApproved, branchCount, activeDevices,
                faultyDevices, txn30, amount30 == null ? BigDecimal.ZERO : amount30, txnPrev30,
                unresolvedFollowUps, daysSince == null ? 999 : daysSince);
    }

    // ---- Device failure-prediction features ----

    /** Deployed/faulty devices worth scoring (in-stock/retired excluded — nothing to predict). */
    public List<UUID> deviceIdsToScore() {
        return jdbc.queryForList(
                "select id from inventory.pos_devices where deleted_at is null "
                        + "and status in ('DEPLOYED','FAULTY','IN_REPAIR')", UUID.class);
    }

    public com.santimpay.posctl.ai.domain.DeviceFeatures deviceFeaturesFor(UUID deviceId) {
        String serial = jdbc.queryForObject(
                "select serial_no from inventory.pos_devices where id = ?", String.class, deviceId);

        // Fault/RMA signal: faults are recorded on the device via the audit log of status changes.
        int faults = (int) count("""
                select count(*) from audit.audit_log
                where schema_name='inventory' and table_name='pos_devices' and row_id = ?
                  and new_data->>'status' in ('FAULTY','IN_REPAIR')
                  and occurred_at >= now() - interval '90 days'""", deviceId);

        // Telemetry over the last 7 days (hot partition window).
        Double offlineRatio = jdbc.queryForObject("""
                select case when count(*)=0 then 0.0
                            else sum((device_status='offline')::int)::float / count(*) end
                from health.device_health_reports
                where device_id = ? and reported_at >= now() - interval '7 days'""",
                Double.class, deviceId);

        Integer avgBattery = jdbc.queryForObject("""
                select coalesce(avg(battery_level)::int, -1)
                from health.device_health_reports
                where device_id = ? and reported_at >= now() - interval '7 days'""",
                Integer.class, deviceId);

        Integer minSignal = jdbc.queryForObject("""
                select coalesce(min(signal_strength), -1)
                from health.device_health_reports
                where device_id = ? and reported_at >= now() - interval '7 days'""",
                Integer.class, deviceId);

        Integer ageMonths = jdbc.queryForObject("""
                select coalesce(
                  extract(month from age(now(), coalesce(production_date, purchased_at)))::int
                  + 12 * extract(year from age(now(), coalesce(production_date, purchased_at)))::int, 0)
                from inventory.pos_devices where id = ?""", Integer.class, deviceId);

        Integer daysSinceSeen = jdbc.queryForObject("""
                select coalesce(extract(day from now() - max(reported_at))::int, 999)
                from health.device_health_reports where device_id = ?""", Integer.class, deviceId);

        return new com.santimpay.posctl.ai.domain.DeviceFeatures(
                deviceId, serial, faults,
                offlineRatio == null ? 0.0 : offlineRatio,
                avgBattery == null ? -1 : avgBattery,
                minSignal == null ? -1 : minSignal,
                ageMonths == null ? 0 : ageMonths,
                daysSinceSeen == null ? 999 : daysSinceSeen);
    }

    private long count(String sql, Object... args) {
        Long n = jdbc.queryForObject(sql, Long.class, args);
        return n == null ? 0L : n;
    }

    /** Convenience for callers that want the whole population's features in one pass. */
    public List<MerchantFeatures> allFeatures() {
        List<MerchantFeatures> out = new ArrayList<>();
        for (UUID id : merchantIdsToScore()) out.add(featuresFor(id));
        return out;
    }
}
