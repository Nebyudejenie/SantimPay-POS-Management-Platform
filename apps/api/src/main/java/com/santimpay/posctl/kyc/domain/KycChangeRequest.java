package com.santimpay.posctl.kyc.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;

/**
 * Merchant KYC change request (the "Merchant's KYC Change Request" form): settlement account and/or
 * trade-name changes. Requires the declaration to be accepted to submit, and is gated by a dual
 * Finance+Compliance approval workflow (see RBAC §10.7).
 */
@Getter
@Entity
@Table(name = "change_requests", schema = "kyc")
public class KycChangeRequest extends AggregateRoot<KycChangeRequest> {

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "terminal_id")
    private String terminalId;

    @Column(name = "current_trade_name")
    private String currentTradeName;

    @Column(name = "owner_full_name")
    private String ownerFullName;

    @Column(name = "owner_phone")
    private String ownerPhone;

    @Column(name = "change_type", nullable = false, updatable = false)
    private String changeType; // settlement_account | trade_name | both | other

    @Column(name = "new_settlement_account")
    private String newSettlementAccount;

    @Column(name = "new_trade_name")
    private String newTradeName;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "declaration_accepted", nullable = false)
    private boolean declarationAccepted;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "merchant_city")
    private String merchantCity;

    @Column(name = "encoder_confirmed", nullable = false)
    private boolean encoderConfirmed;

    @Column(name = "status", nullable = false)
    private String status; // submitted | under_review | approved | rejected | applied

    protected KycChangeRequest() {}

    public static KycChangeRequest submit(UUID merchantId, String changeType, String reason,
                                          boolean declarationAccepted) {
        if (!declarationAccepted) {
            throw DomainException.invalidState("Declaration must be accepted to submit a change request");
        }
        KycChangeRequest c = new KycChangeRequest();
        c.assignIdentityIfAbsent();
        c.merchantId = merchantId;
        c.changeType = changeType;
        c.reason = reason;
        c.declarationAccepted = true;
        c.status = "submitted";
        return c;
    }
}
