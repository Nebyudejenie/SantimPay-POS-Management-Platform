package com.santimpay.posctl.analytics.web;

import com.santimpay.posctl.analytics.application.AnalyticsService;
import com.santimpay.posctl.analytics.application.DashboardView;
import com.santimpay.posctl.analytics.domain.MonthlyTransactionSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Analytics")
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    @Operation(summary = "Executive dashboard counters")
    @GetMapping("/dashboard")
    public DashboardView dashboard() {
        return service.dashboard();
    }

    public record MonthlyTxnRow(String terminalId, String terminalName, UUID merchantId,
                                long totalTxnCount, BigDecimal totalTxnAmount,
                                BigDecimal santimpayCommission, BigDecimal totalCommissionBr,
                                BigDecimal totalCommissionCut, String currency) {}

    @Operation(summary = "Monthly transaction + commission report")
    @GetMapping("/transactions/monthly")
    public List<MonthlyTxnRow> monthly(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month,
            @RequestParam(required = false) UUID merchantId) {
        return service.monthlyTransactions(month, merchantId).stream()
                .map(m -> new MonthlyTxnRow(m.getTerminalId(), m.getTerminalName(), m.getMerchantId(),
                        m.getTotalTxnCount(), m.getTotalTxnAmount(), m.getSantimpayCommission(),
                        m.getTotalCommissionBr(), m.getTotalCommissionCut(), m.getCurrency()))
                .toList();
    }
}
