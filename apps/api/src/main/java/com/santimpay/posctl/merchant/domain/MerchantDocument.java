package com.santimpay.posctl.merchant.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/** Merchant document — licenses, registrations, certificates, proof of address. */
@Getter
@Entity
@Table(name = "merchant_documents", schema = "merchant")
public class MerchantDocument extends AggregateRoot<MerchantDocument> {

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "document_type", nullable = false)
    private String documentType;  // business_license, tax_certificate, bank_statement, proof_of_address

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "issuing_authority")
    private String issuingAuthority;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verification_notes")
    private String verificationNotes;

    @Column(name = "is_current")
    private boolean isCurrent;

    protected MerchantDocument() {}

    public static MerchantDocument submit(UUID merchantId, String documentType, String documentNumber,
                                          LocalDate issueDate, LocalDate expiryDate, String filePath) {
        if (merchantId == null || documentType == null) {
            throw DomainException.invalidState("merchantId and documentType required");
        }
        MerchantDocument doc = new MerchantDocument();
        doc.assignIdentityIfAbsent();
        doc.merchantId = merchantId;
        doc.documentType = documentType;
        doc.documentNumber = documentNumber;
        doc.issueDate = issueDate;
        doc.expiryDate = expiryDate;
        doc.filePath = filePath;
        doc.isCurrent = true;
        return doc;
    }

    public void markVerified(UUID verifiedBy, String notes) {
        this.verifiedBy = verifiedBy;
        this.verifiedAt = Instant.now();
        this.verificationNotes = notes;
    }

    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }
}
