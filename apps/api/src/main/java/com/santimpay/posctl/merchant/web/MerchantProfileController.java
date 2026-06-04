package com.santimpay.posctl.merchant.web;

import com.santimpay.posctl.merchant.domain.Bank;
import com.santimpay.posctl.merchant.domain.MerchantOwner;
import com.santimpay.posctl.merchant.domain.SettlementAccount;
import com.santimpay.posctl.merchant.infrastructure.BankJpaRepository;
import com.santimpay.posctl.merchant.infrastructure.OwnerJpaRepository;
import com.santimpay.posctl.merchant.infrastructure.SettlementJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Merchant profile sub-resources: owners (KYC subjects), settlement accounts (payout banks), and the
 * banks reference list. Completes the merchant information structure beyond core fields + branches.
 * Note: package-visible JPA repos are used directly here (read + simple create) — these are thin
 * CRUD sub-resources, not behavior-rich aggregates, so a dedicated service layer would be ceremony.
 */
@Tag(name = "Merchant Profile")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MerchantProfileController {

    private final OwnerJpaRepository owners;
    private final SettlementJpaRepository settlements;
    private final BankJpaRepository banks;

    // ---------- Owners ----------
    public record CreateOwnerRequest(@NotBlank String fullName, String nationalId, String phone,
                                     String email, BigDecimal ownershipPct, boolean primary) {}
    public record OwnerResponse(UUID id, String fullName, String nationalId, String phone,
                                String email, BigDecimal ownershipPct, boolean primary) {}

    @Operation(summary = "List a merchant's owners")
    @GetMapping("/merchants/{merchantId}/owners")
    @PreAuthorize("hasAuthority('PERM_merchant:read')")
    public List<OwnerResponse> listOwners(@PathVariable UUID merchantId) {
        return owners.findByMerchant(merchantId).stream()
                .map(o -> new OwnerResponse(o.getId(), o.getFullName(), o.getNationalId(), o.getPhone(),
                        o.getEmail(), o.getOwnershipPct(), o.isPrimary())).toList();
    }

    @Operation(summary = "Add an owner to a merchant")
    @PostMapping("/merchants/{merchantId}/owners")
    @PreAuthorize("hasAuthority('PERM_merchant:create')")
    @Transactional
    public ResponseEntity<OwnerResponse> addOwner(@PathVariable UUID merchantId,
                                                  @Valid @RequestBody CreateOwnerRequest r) {
        MerchantOwner o = owners.save(MerchantOwner.create(merchantId, r.fullName(), r.nationalId(),
                r.phone(), r.email(), r.ownershipPct(), r.primary()));
        return ResponseEntity.status(201).body(new OwnerResponse(o.getId(), o.getFullName(),
                o.getNationalId(), o.getPhone(), o.getEmail(), o.getOwnershipPct(), o.isPrimary()));
    }

    // ---------- Settlement accounts ----------
    public record CreateSettlementRequest(@NotNull UUID bankId, @NotBlank String accountNo,
                                          @NotBlank String accountName, String currency, boolean primary) {}
    public record SettlementResponse(UUID id, UUID bankId, String bankName, String accountNo,
                                     String accountName, String currency, boolean primary, boolean verified) {}

    @Operation(summary = "List a merchant's settlement accounts")
    @GetMapping("/merchants/{merchantId}/settlement-accounts")
    @PreAuthorize("hasAuthority('PERM_merchant:read')")
    public List<SettlementResponse> listSettlements(@PathVariable UUID merchantId) {
        return settlements.findByMerchant(merchantId).stream().map(s -> {
            String bankName = banks.findById(s.getBankId()).map(Bank::getName).orElse("?");
            return new SettlementResponse(s.getId(), s.getBankId(), bankName, s.getAccountNo(),
                    s.getAccountName(), s.getCurrency(), s.isPrimary(), s.getVerifiedAt() != null);
        }).toList();
    }

    @Operation(summary = "Add a settlement account to a merchant")
    @PostMapping("/merchants/{merchantId}/settlement-accounts")
    @PreAuthorize("hasAuthority('PERM_merchant:update')")
    @Transactional
    public ResponseEntity<SettlementResponse> addSettlement(@PathVariable UUID merchantId,
                                                            @Valid @RequestBody CreateSettlementRequest r) {
        SettlementAccount s = settlements.save(SettlementAccount.create(merchantId, r.bankId(),
                r.accountNo(), r.accountName(), r.currency(), r.primary()));
        String bankName = banks.findById(s.getBankId()).map(Bank::getName).orElse("?");
        return ResponseEntity.status(201).body(new SettlementResponse(s.getId(), s.getBankId(), bankName,
                s.getAccountNo(), s.getAccountName(), s.getCurrency(), s.isPrimary(), false));
    }

    // ---------- Banks reference ----------
    public record BankResponse(UUID id, String code, String name) {}

    @Operation(summary = "List active banks (reference data for settlement accounts)")
    @GetMapping("/banks")
    @PreAuthorize("hasAuthority('PERM_merchant:read')")
    public List<BankResponse> listBanks() {
        return banks.findByActiveTrueOrderByName().stream()
                .map(b -> new BankResponse(b.getId(), b.getCode(), b.getName())).toList();
    }
}
