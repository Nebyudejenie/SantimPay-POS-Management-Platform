package com.santimpay.posctl.merchant.web;

import com.santimpay.posctl.merchant.application.MerchantService;
import com.santimpay.posctl.merchant.application.OnboardMerchantCommand;
import com.santimpay.posctl.merchant.domain.Merchant;
import com.santimpay.posctl.merchant.domain.MerchantStatus;
import com.santimpay.posctl.merchant.web.MerchantRequests.OnboardMerchantRequest;
import com.santimpay.posctl.merchant.web.MerchantRequests.SuspendMerchantRequest;
import com.santimpay.posctl.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Merchant REST API (docs/04). Validation + mapping at the edge; authorization enforced in the
 * service via {@code @PreAuthorize}. Action endpoints use the {@code :verb} convention.
 */
@Tag(name = "Merchants")
@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService service;
    private final MerchantWebMapper mapper;

    @Operation(summary = "Onboard a new merchant")
    @PostMapping
    public ResponseEntity<MerchantResponse> onboard(@Valid @RequestBody OnboardMerchantRequest req) {
        Merchant merchant = service.onboard(new OnboardMerchantCommand(
                req.merchantNo(), req.legalName(), req.tradeName(), req.taxId(), req.category()));
        return ResponseEntity
                .created(URI.create("/api/v1/merchants/" + merchant.getId()))
                .body(mapper.toResponse(merchant));
    }

    @Operation(summary = "Get a merchant by id")
    @GetMapping("/{id}")
    public MerchantResponse get(@PathVariable UUID id) {
        return mapper.toResponse(service.get(id));
    }

    @Operation(summary = "Search / list merchants")
    @GetMapping
    public PageResponse<MerchantResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) MerchantStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {
        Page<Merchant> result = service.search(q, status,
                PageRequest.of(page, Math.min(limit, 200), Sort.by(Sort.Direction.DESC, "id")));
        return PageResponse.from(result, result.map(mapper::toResponse).getContent());
    }

    @Operation(summary = "Activate a merchant (workflow-gated, requires merchant:approve)")
    @PostMapping("/{id}:activate")
    public MerchantResponse activate(@PathVariable UUID id) {
        return mapper.toResponse(service.activate(id));
    }

    @Operation(summary = "Suspend an active merchant")
    @PostMapping("/{id}:suspend")
    public MerchantResponse suspend(@PathVariable UUID id,
                                    @Valid @RequestBody SuspendMerchantRequest req) {
        return mapper.toResponse(service.suspend(id, req.reason()));
    }
}
