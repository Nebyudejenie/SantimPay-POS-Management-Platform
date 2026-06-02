package com.santimpay.posctl.analytics.application;

import com.santimpay.posctl.analytics.domain.MonthlyTransactionSummary;
import com.santimpay.posctl.analytics.infrastructure.AnalyticsReadModel;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-side service (CQRS query side). Serves dashboards and the monthly transaction-commission
 * report from read models / projections. Deliberately read-only and cross-cutting: it queries
 * lightweight projections rather than loading other modules' aggregates. In production these run
 * against the read replica.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsReadModel readModel;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_report:read')")
    public DashboardView dashboard() {
        return readModel.loadDashboard();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_report:read')")
    public List<MonthlyTransactionSummary> monthlyTransactions(LocalDate month, UUID merchantId) {
        return readModel.monthlyTransactions(month, merchantId);
    }
}
