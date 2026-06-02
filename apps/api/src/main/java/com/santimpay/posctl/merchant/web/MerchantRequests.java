package com.santimpay.posctl.merchant.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Web request DTOs for the merchant API. Bean Validation runs before the use case is invoked. */
public final class MerchantRequests {

    private MerchantRequests() {}

    public record OnboardMerchantRequest(
            @NotBlank @Size(max = 40) String merchantNo,
            @NotBlank @Size(max = 200) String legalName,
            @Size(max = 200) String tradeName,
            @Size(max = 40) String taxId,
            @Size(max = 80) String category) {}

    public record SuspendMerchantRequest(
            @NotBlank @Size(max = 500) String reason) {}
}
