package com.santimpay.posctl.merchant.web;

import com.santimpay.posctl.merchant.domain.*;
import com.santimpay.posctl.merchant.domain.*;
import com.santimpay.posctl.merchant.infrastructure.*;
import com.santimpay.posctl.shared.domain.Attachment;
import com.santimpay.posctl.shared.infrastructure.AttachmentJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/** Merchant data — documents, compliance, stakeholders, settlement history, attachments. */
@Tag(name = "Merchant Data & Compliance")
@RestController
@RequestMapping("/api/v1/merchants/{merchantId}")
@RequiredArgsConstructor
public class MerchantDataController {

    private final MerchantDocumentJpaRepository documents;
    private final ComplianceChecklistJpaRepository compliance;
    private final MerchantStakeholderJpaRepository stakeholders;
    private final SettlementHistoryJpaRepository settlements;
    private final AttachmentJpaRepository attachments;

    // ========== DOCUMENTS ==========
    public record DocumentResponse(UUID id, String documentType, String documentNumber, LocalDate expiryDate, boolean verified, boolean expired) {}
    public record CreateDocumentRequest(@NotBlank String documentType, String documentNumber, LocalDate issueDate, LocalDate expiryDate) {}

    @Operation(summary = "List merchant documents (licenses, certificates, etc.)")
    @GetMapping("/documents")
    @PreAuthorize("hasAuthority('PERM_merchant:documents')")
    public List<DocumentResponse> listDocuments(@PathVariable UUID merchantId) {
        return documents.findByMerchant(merchantId).stream()
                .map(d -> new DocumentResponse(d.getId(), d.getDocumentType(), d.getDocumentNumber(),
                        d.getExpiryDate(), d.getVerifiedAt() != null, d.isExpired()))
                .toList();
    }

    @Operation(summary = "Submit a merchant document")
    @PostMapping("/documents")
    @PreAuthorize("hasAuthority('PERM_merchant:create')")
    @Transactional
    public ResponseEntity<DocumentResponse> submitDocument(@PathVariable UUID merchantId,
                                                          @Valid @RequestBody CreateDocumentRequest req) {
        MerchantDocument doc = documents.save(MerchantDocument.submit(merchantId, req.documentType(),
                req.documentNumber(), req.issueDate(), req.expiryDate(), null));
        return ResponseEntity.status(201).body(new DocumentResponse(doc.getId(), doc.getDocumentType(),
                doc.getDocumentNumber(), doc.getExpiryDate(), false, doc.isExpired()));
    }

    // ========== COMPLIANCE ==========
    public record ComplianceResponse(UUID id, String checkType, String status, LocalDate expiryDate, boolean expired) {}

    @Operation(summary = "View merchant compliance checklist")
    @GetMapping("/compliance")
    @PreAuthorize("hasAuthority('PERM_merchant:compliance')")
    public List<ComplianceResponse> listCompliance(@PathVariable UUID merchantId) {
        return compliance.findByMerchant(merchantId).stream()
                .map(c -> new ComplianceResponse(c.getId(), c.getCheckType(), c.getStatus(),
                        c.getExpiryDate(), c.isExpired()))
                .toList();
    }

    @Operation(summary = "Mark a compliance check as passed")
    @PostMapping("/compliance/{checkId}:pass")
    @PreAuthorize("hasAuthority('PERM_merchant:compliance')")
    @Transactional
    public ResponseEntity<Void> passCompliance(@PathVariable UUID merchantId, @PathVariable UUID checkId,
                                               @RequestParam(required = false) LocalDate expiryDate) {
        compliance.findById(checkId).ifPresent(c -> {
            c.markPassed(null, expiryDate);
            compliance.save(c);
        });
        return ResponseEntity.ok().build();
    }

    // ========== STAKEHOLDERS ==========
    public record StakeholderResponse(UUID id, String fullName, String role, String phone, String email, boolean isPrimary) {}
    public record CreateStakeholderRequest(@NotBlank String fullName, @NotBlank String role, String phone, String email) {}

    @Operation(summary = "List merchant stakeholders (owners, operators, signatories, etc.)")
    @GetMapping("/stakeholders")
    @PreAuthorize("hasAuthority('PERM_merchant:read')")
    public List<StakeholderResponse> listStakeholders(@PathVariable UUID merchantId) {
        return stakeholders.findByMerchant(merchantId).stream()
                .filter(MerchantStakeholder::isActive)
                .map(s -> new StakeholderResponse(s.getId(), s.getFullName(), s.getRole(), s.getPhone(), s.getEmail(), s.isPrimary()))
                .toList();
    }

    @Operation(summary = "Add a merchant stakeholder")
    @PostMapping("/stakeholders")
    @PreAuthorize("hasAuthority('PERM_merchant:create')")
    @Transactional
    public ResponseEntity<StakeholderResponse> addStakeholder(@PathVariable UUID merchantId,
                                                             @Valid @RequestBody CreateStakeholderRequest req) {
        MerchantStakeholder s = stakeholders.save(MerchantStakeholder.register(merchantId, req.fullName(), req.role()));
        return ResponseEntity.status(201).body(new StakeholderResponse(s.getId(), s.getFullName(), s.getRole(),
                s.getPhone(), s.getEmail(), s.isPrimary()));
    }

    // ========== SETTLEMENT HISTORY ==========
    public record SettlementResponse(UUID id, LocalDate periodFrom, LocalDate periodTo, java.math.BigDecimal grossAmount,
                                     java.math.BigDecimal netAmount, String status, String bankReference) {}

    @Operation(summary = "View merchant settlement history (earnings)")
    @GetMapping("/settlement-history")
    @PreAuthorize("hasAuthority('PERM_settlement:view_history')")
    public Page<SettlementResponse> listSettlements(@PathVariable UUID merchantId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int limit) {
        Page<SettlementHistory> result = settlements.findByMerchant(merchantId, PageRequest.of(page, limit));
        return result.map(s -> new SettlementResponse(s.getId(), s.getPeriodFrom(), s.getPeriodTo(),
                s.getGrossAmount(), s.getNetAmount(), s.getStatus(), s.getBankReference()));
    }

    // ========== ATTACHMENTS ==========
    public record AttachmentResponse(UUID id, String documentType, String fileName, String filePath, boolean verified) {}

    @Operation(summary = "List merchant attachments (documents, photos, signatures)")
    @GetMapping("/attachments")
    @PreAuthorize("hasAuthority('PERM_merchant:read')")
    public List<AttachmentResponse> listAttachments(@PathVariable UUID merchantId) {
        return attachments.findByEntity("merchant", merchantId).stream()
                .map(a -> new AttachmentResponse(a.getId(), a.getDocumentType(), a.getFileName(),
                        a.getFilePath(), a.getVerifiedAt() != null))
                .toList();
    }

    @Operation(summary = "Upload merchant attachment (document, photo, signature)")
    @PostMapping("/attachments")
    @PreAuthorize("hasAuthority('PERM_attachment:upload')")
    @Transactional
    public ResponseEntity<AttachmentResponse> uploadAttachment(@PathVariable UUID merchantId,
                                                              @RequestParam @NotBlank String documentType,
                                                              @RequestParam @NotBlank String fileName,
                                                              @RequestParam @NotBlank String filePath) {
        Attachment a = attachments.save(Attachment.upload("merchant", merchantId, documentType, fileName, null, null, filePath));
        return ResponseEntity.status(201).body(new AttachmentResponse(a.getId(), a.getDocumentType(), a.getFileName(), a.getFilePath(), false));
    }
}
