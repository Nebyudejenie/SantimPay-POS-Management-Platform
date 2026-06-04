package com.santimpay.posctl.merchant.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/**
 * A merchant settlement (payout) bank account — where funds are settled. References a Bank by id.
 * One can be primary. Verification is recorded for compliance.
 */
@Getter
@Entity
@Table(name = "settlement_accounts", schema = "merchant")
public class SettlementAccount extends AggregateRoot<SettlementAccount> {

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "bank_id", nullable = false)
    private UUID bankId;

    @Column(name = "account_no", nullable = false)
    private String accountNo;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    protected SettlementAccount() {}

    public static SettlementAccount create(UUID merchantId, UUID bankId, String accountNo,
                                           String accountName, String currency, boolean primary) {
        if (merchantId == null || bankId == null) throw DomainException.invalidState("merchantId and bankId required");
        if (accountNo == null || accountNo.isBlank()) throw DomainException.invalidState("accountNo required");
        if (accountName == null || accountName.isBlank()) throw DomainException.invalidState("accountName required");
        SettlementAccount a = new SettlementAccount();
        a.assignIdentityIfAbsent();
        a.merchantId = merchantId;
        a.bankId = bankId;
        a.accountNo = accountNo;
        a.accountName = accountName;
        a.currency = currency == null ? "ETB" : currency;
        a.primary = primary;
        return a;
    }

    public void markVerified() { this.verifiedAt = Instant.now(); }
}
