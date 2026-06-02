package com.santimpay.posctl.kyc.web;

import com.santimpay.posctl.kyc.application.KycService;
import com.santimpay.posctl.kyc.domain.KycRequest;
import com.santimpay.posctl.kyc.domain.KycStatus;
import com.santimpay.posctl.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "KYC")
@RestController
@RequestMapping("/api/v1/kyc-requests")
@RequiredArgsConstructor
public class KycController {

    private final KycService service;

    public record KycResponse(UUID id, UUID merchantId, String requestType, String status,
                              UUID reviewerId, String decisionReason, Instant submittedAt,
                              Instant decidedAt) {}

    public record RejectRequest(@NotBlank @Size(max = 1000) String reason) {}

    private KycResponse toResponse(KycRequest k) {
        return new KycResponse(k.getId(), k.getMerchantId(), k.getRequestType(), k.getStatus().name(),
                k.getReviewerId(), k.getDecisionReason(), k.getSubmittedAt(), k.getDecidedAt());
    }

    @Operation(summary = "Get a KYC request")
    @GetMapping("/{id}")
    public KycResponse get(@PathVariable UUID id) {
        return toResponse(service.get(id));
    }

    @Operation(summary = "List KYC requests")
    @GetMapping
    public PageResponse<KycResponse> list(
            @RequestParam(required = false) KycStatus status,
            @RequestParam(required = false) UUID merchantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {
        Page<KycRequest> result = service.search(status, merchantId,
                PageRequest.of(page, Math.min(limit, 200), Sort.by(Sort.Direction.DESC, "id")));
        return PageResponse.from(result, result.map(this::toResponse).getContent());
    }

    @Operation(summary = "Assign the request to me for review")
    @PostMapping("/{id}:assign")
    public KycResponse assign(@PathVariable UUID id) {
        return toResponse(service.assignToMe(id));
    }

    @Operation(summary = "Approve a KYC request (initiates merchant-activation workflow)")
    @PostMapping("/{id}:approve")
    public KycResponse approve(@PathVariable UUID id) {
        return toResponse(service.approve(id));
    }

    @Operation(summary = "Reject a KYC request")
    @PostMapping("/{id}:reject")
    public KycResponse reject(@PathVariable UUID id, @Valid @RequestBody RejectRequest req) {
        return toResponse(service.reject(id, req.reason()));
    }
}
