package com.santimpay.posctl.followup.web;

import com.santimpay.posctl.followup.application.FollowUpService;
import com.santimpay.posctl.followup.domain.FollowUp;
import com.santimpay.posctl.followup.domain.FollowUpChannel;
import com.santimpay.posctl.followup.domain.FollowUpOutcome;
import com.santimpay.posctl.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
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

@Tag(name = "Follow-ups")
@RestController
@RequestMapping("/api/v1/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService service;

    public record LogFollowUpRequest(
            UUID merchantId,
            @NotNull FollowUpChannel channel,
            FollowUpOutcome outcome,
            @Size(max = 4000) String notes,
            @Size(max = 160) String contactedPerson,
            @Size(max = 40) String contactedPhone,
            Instant nextActionAt) {}

    public record FollowUpResponse(UUID id, UUID merchantId, UUID agentId, String channel,
                                   String outcome, String notes, String contactedPerson,
                                   String contactedPhone, boolean aiGenerated, Instant contactedAt,
                                   Instant nextActionAt) {}

    private FollowUpResponse toResponse(FollowUp f) {
        return new FollowUpResponse(f.getId(), f.getMerchantId(), f.getAgentId(),
                f.getChannel().name(), f.getOutcome() == null ? null : f.getOutcome().name(),
                f.getNotes(), f.getContactedPerson(), f.getContactedPhone(), f.isAiGenerated(),
                f.getContactedAt(), f.getNextActionAt());
    }

    @Operation(summary = "Log a follow-up contact")
    @PostMapping
    public ResponseEntity<FollowUpResponse> log(@Valid @RequestBody LogFollowUpRequest req) {
        FollowUp f = service.log(req.merchantId(), req.channel(), req.outcome(), req.notes(),
                req.contactedPerson(), req.contactedPhone(), req.nextActionAt());
        return ResponseEntity.created(java.net.URI.create("/api/v1/follow-ups/" + f.getId()))
                .body(toResponse(f));
    }

    @Operation(summary = "List follow-ups (filter by merchant/agent)")
    @GetMapping
    public PageResponse<FollowUpResponse> list(
            @RequestParam(required = false) UUID merchantId,
            @RequestParam(required = false) UUID agent,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {
        Page<FollowUp> result = service.search(merchantId, agent,
                PageRequest.of(page, Math.min(limit, 200), Sort.by(Sort.Direction.DESC, "id")));
        return PageResponse.from(result, result.map(this::toResponse).getContent());
    }
}
