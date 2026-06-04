package com.santimpay.posctl.merchant.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;

/** Settlement summary for a merchant — what they earned in a period. */
@Getter
@Entity
@Table(name = "settlement_history", schema = "merchant")
public class SettlementHistory extends AggregateRoot<SettlementHistory> {

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "settlement_account_id")
    private UUID settlementAccountId;

    @Column(name = "settlement_period_from", nullable = false)
    private LocalDate periodFrom;

    @Column(name = "settlement_period_to", nullable = false)
    private LocalDate periodTo;

    @Column(name = "gross_amount", nullable = false)
    private BigDecimal grossAmount;

    @Column(name = "commission_amount", nullable = false)
    private BigDecimal commissionAmount;

    @Column(name = "net_amount", nullable = false)
    private BigDecimal netAmount;

    @Column(name = "transaction_count")
    private Integer transactionCount;

    @Column(name = "settlement_status", nullable = false)
    private String status;  // PENDING, IN_PROGRESS, COMPLETED, FAILED, REVERSED

    @Column(name = "transferred_at")
    private java.time.Instant transferredAt;

    @Column(name = "transferred_by")
    private UUID transferredBy;

    @Column(name = "bank_reference")
    private String bankReference;

    @Column(name = "notes")
    private String notes;

    protected SettlementHistory() {}
    public UUID getMerchantId() { return merchantId; }
    public UUID getSettlementAccountId() { return settlementAccountId; }
    public LocalDate getPeriodFrom() { return periodFrom; }
    public LocalDate getPeriodTo() { return periodTo; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public BigDecimal getNetAmount() { return netAmount; }
    public Integer getTransactionCount() { return transactionCount; }
    public String getStatus() { return status; }
    public Instant getTransferredAt() { return transferredAt; }
    public UUID getTransferredBy() { return transferredBy; }
    public String getBankReference() { return bankReference; }
    public String getNotes() { return notes; }

    public static SettlementHistory create(UUID merchantId, LocalDate periodFrom, LocalDate periodTo,
                                           BigDecimal grossAmount, BigDecimal commissionAmount) {
        if (merchantId == null || periodFrom == null || periodTo == null || grossAmount == null) {
            throw DomainException.invalidState("merchantId, periods, and amounts required");
        }
        SettlementHistory h = new SettlementHistory();
        h.assignIdentityIfAbsent();
        h.merchantId = merchantId;
        h.periodFrom = periodFrom;
        h.periodTo = periodTo;
        h.grossAmount = grossAmount;
        h.commissionAmount = commissionAmount;
        h.netAmount = grossAmount.subtract(commissionAmount);
        h.status = "PENDING";
        return h;
    }

    public void markCompleted(UUID transferredBy, String bankReference) {
        this.status = "COMPLETED";
        this.transferredBy = transferredBy;
        this.transferredAt = java.time.Instant.now();
        this.bankReference = bankReference;
    }
}
