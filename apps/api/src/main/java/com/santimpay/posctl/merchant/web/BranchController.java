package com.santimpay.posctl.merchant.web;

import com.santimpay.posctl.merchant.application.BranchService;
import com.santimpay.posctl.merchant.domain.Branch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Merchant branch intake — the DATA_ENCODER's create surface. Nested under a merchant. */
@Tag(name = "Branches")
@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService service;

    public record CreateBranchRequest(
            @NotBlank @Size(max = 40) String branchNo,
            @NotBlank @Size(max = 200) String name,
            @Size(max = 80) String region,
            @Size(max = 80) String city,
            @Size(max = 80) String subCity,
            @Size(max = 80) String woreda,
            @Size(max = 300) String addressLine,
            @Size(max = 40) String contactPhone,
            Double latitude, Double longitude) {}

    public record BranchResponse(UUID id, UUID merchantId, String branchNo, String name,
                                 String region, String city, String subCity, String woreda,
                                 String addressLine, String contactPhone, String status) {}

    private BranchResponse toResponse(Branch b) {
        return new BranchResponse(b.getId(), b.getMerchantId(), b.getBranchNo(), b.getName(),
                b.getRegion(), b.getCity(), b.getSubCity(), b.getWoreda(), b.getAddressLine(),
                b.getContactPhone(), b.getStatus());
    }

    @Operation(summary = "List a merchant's branches")
    @GetMapping
    public List<BranchResponse> list(@PathVariable UUID merchantId) {
        return service.list(merchantId).stream().map(this::toResponse).toList();
    }

    @Operation(summary = "Add a branch to a merchant (data-entry intake)")
    @PostMapping
    public ResponseEntity<BranchResponse> add(@PathVariable UUID merchantId,
                                              @Valid @RequestBody CreateBranchRequest req) {
        Branch b = service.add(merchantId, req.branchNo(), req.name(), req.region(), req.city(),
                req.subCity(), req.woreda(), req.addressLine(), req.contactPhone(),
                req.latitude(), req.longitude());
        return ResponseEntity
                .created(java.net.URI.create("/api/v1/merchants/" + merchantId + "/branches/" + b.getId()))
                .body(toResponse(b));
    }
}
