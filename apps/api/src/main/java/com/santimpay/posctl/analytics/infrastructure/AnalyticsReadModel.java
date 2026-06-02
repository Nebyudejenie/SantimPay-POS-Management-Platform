package com.santimpay.posctl.analytics.infrastructure;

import com.santimpay.posctl.analytics.application.DashboardView;
import com.santimpay.posctl.analytics.domain.MonthlyTransactionSummary;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The read side's data access. Uses plain SQL against schemas it does NOT own — this is the one
 * sanctioned cross-schema read path (reporting), kept read-only and isolated here so it doesn't leak
 * into other modules. In production these queries target a read replica and/or materialized views
 * ({@code analytics.mv_*}); here we read base tables directly for simplicity.
 */
@Component
@RequiredArgsConstructor
public class AnalyticsReadModel {

    private final JdbcTemplate jdbc;
    private final EntityManager em;

    public DashboardView loadDashboard() {
        long totalMerchants = count("select count(*) from merchant.merchants where deleted_at is null");
        long activeMerchants = count("select count(*) from merchant.merchants where status='ACTIVE' and deleted_at is null");
        long inStock = count("select count(*) from inventory.pos_devices where status='IN_STOCK' and deleted_at is null");
        long deployed = count("select count(*) from inventory.pos_devices where status='DEPLOYED' and deleted_at is null");
        long faulty = count("select count(*) from inventory.pos_devices where status='FAULTY' and deleted_at is null");
        long deploymentsToday = count("select count(*) from deployment.deployments where scheduled_date=current_date and deleted_at is null");
        long openTasks = count("select count(*) from tasks.tasks where status not in ('DONE','CANCELLED') and deleted_at is null");
        long pendingKyc = count("select count(*) from kyc.kyc_requests where status in ('SUBMITTED','UNDER_REVIEW','PENDING_DOCS') and deleted_at is null");
        return new DashboardView(totalMerchants, activeMerchants, inStock, deployed, faulty,
                deploymentsToday, openTasks, pendingKyc);
    }

    public List<MonthlyTransactionSummary> monthlyTransactions(LocalDate month, UUID merchantId) {
        StringBuilder ql = new StringBuilder(
                "select m from MonthlyTransactionSummary m where 1=1");
        if (month != null) ql.append(" and m.periodMonth = :month");
        if (merchantId != null) ql.append(" and m.merchantId = :merchantId");
        ql.append(" order by m.totalTxnAmount desc");
        var q = em.createQuery(ql.toString(), MonthlyTransactionSummary.class);
        if (month != null) q.setParameter("month", month.withDayOfMonth(1));
        if (merchantId != null) q.setParameter("merchantId", merchantId);
        return q.setMaxResults(1000).getResultList();
    }

    private long count(String sql) {
        Long n = jdbc.queryForObject(sql, Long.class);
        return n == null ? 0L : n;
    }
}
