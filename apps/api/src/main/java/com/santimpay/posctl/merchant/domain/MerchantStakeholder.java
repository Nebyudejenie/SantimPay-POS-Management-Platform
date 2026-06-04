package com.santimpay.posctl.merchant.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;

/** Merchant stakeholder — owner, operator, financial signatory, technical contact, etc. */
@Getter
@Entity
@Table(name = "merchant_stakeholders", schema = "merchant")
public class MerchantStakeholder extends AggregateRoot<MerchantStakeholder> {

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "role", nullable = false)
    private String role;  // owner, operator, financial_signatory, technical_contact, manager, accountant

    @Column(name = "national_id")
    private String nationalId;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "is_primary")
    private boolean isPrimary;

    @Column(name = "active_from")
    private LocalDate activeFrom;

    @Column(name = "active_until")
    private LocalDate activeUntil;

    protected MerchantStakeholder() {}
    public UUID getMerchantId() { return merchantId; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getNationalId() { return nationalId; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public LocalDate getActiveFrom() { return activeFrom; }
    public LocalDate getActiveUntil() { return activeUntil; }

    public static MerchantStakeholder register(UUID merchantId, String fullName, String role) {
        if (merchantId == null || fullName == null || fullName.isBlank() || role == null || role.isBlank()) {
            throw DomainException.invalidState("merchantId, fullName, and role required");
        }
        MerchantStakeholder s = new MerchantStakeholder();
        s.assignIdentityIfAbsent();
        s.merchantId = merchantId;
        s.fullName = fullName;
        s.role = role;
        s.activeFrom = LocalDate.now();
        return s;
    }

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return (activeFrom == null || !today.isBefore(activeFrom)) &&
               (activeUntil == null || !today.isAfter(activeUntil));
    }
}
