package com.santimpay.posctl.analytics.domain;

import com.santimpay.posctl.shared.domain.UuidV7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;

/**
 * Monthly per-terminal transaction + commission rollup (the "Monthly POS Transaction Report").
 * Ingested as reference data from the payment switch (ADR-006) — this platform is NOT a ledger.
 * Append/upsert by (period_month, terminal_id); partitioned monthly. No audit/soft-delete: it is
 * derived reporting data, re-ingestible from source.
 */
@Getter
@Entity
@Table(name = "monthly_transaction_summary", schema = "analytics")
public class MonthlyTransactionSummary {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "period_month", nullable = false)
    private LocalDate periodMonth;       // first day of the month

    @Column(name = "terminal_id", nullable = false)
    private String terminalId;

    @Column(name = "terminal_name")
    private String terminalName;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "total_purchase_count", nullable = false)
    private long totalPurchaseCount;

    @Column(name = "total_purchase_amount", nullable = false)
    private BigDecimal totalPurchaseAmount;

    @Column(name = "gateway_txn_count", nullable = false)
    private long gatewayTxnCount;

    @Column(name = "gateway_txn_amount", nullable = false)
    private BigDecimal gatewayTxnAmount;

    @Column(name = "total_txn_count", nullable = false)
    private long totalTxnCount;

    @Column(name = "total_txn_amount", nullable = false)
    private BigDecimal totalTxnAmount;

    @Column(name = "santimpay_commission", nullable = false)
    private BigDecimal santimpayCommission;

    @Column(name = "total_commission_br", nullable = false)
    private BigDecimal totalCommissionBr;

    @Column(name = "total_commission_cut", nullable = false)
    private BigDecimal totalCommissionCut;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "source_ref")
    private String sourceRef;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    protected MonthlyTransactionSummary() {}

    public static MonthlyTransactionSummary of(LocalDate periodMonth, String terminalId,
            String terminalName, UUID merchantId, long totalPurchaseCount,
            BigDecimal totalPurchaseAmount, long gatewayTxnCount, BigDecimal gatewayTxnAmount,
            long totalTxnCount, BigDecimal totalTxnAmount, BigDecimal santimpayCommission,
            BigDecimal totalCommissionBr, BigDecimal totalCommissionCut, String currency,
            String sourceRef) {
        MonthlyTransactionSummary m = new MonthlyTransactionSummary();
        m.id = UuidV7.generate();
        m.periodMonth = periodMonth;
        m.terminalId = terminalId;
        m.terminalName = terminalName;
        m.merchantId = merchantId;
        m.totalPurchaseCount = totalPurchaseCount;
        m.totalPurchaseAmount = nz(totalPurchaseAmount);
        m.gatewayTxnCount = gatewayTxnCount;
        m.gatewayTxnAmount = nz(gatewayTxnAmount);
        m.totalTxnCount = totalTxnCount;
        m.totalTxnAmount = nz(totalTxnAmount);
        m.santimpayCommission = nz(santimpayCommission);
        m.totalCommissionBr = nz(totalCommissionBr);
        m.totalCommissionCut = nz(totalCommissionCut);
        m.currency = currency == null ? "ETB" : currency;
        m.sourceRef = sourceRef;
        m.ingestedAt = Instant.now();
        return m;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
