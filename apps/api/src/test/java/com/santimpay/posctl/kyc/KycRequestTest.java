package com.santimpay.posctl.kyc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.santimpay.posctl.kyc.domain.KycRequest;
import com.santimpay.posctl.kyc.domain.KycStatus;
import com.santimpay.posctl.kyc.events.KycApproved;
import com.santimpay.posctl.shared.domain.DomainException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class KycRequestTest {

    @Test
    void onboarding_review_approve_raisesEvent() {
        KycRequest k = KycRequest.openOnboarding(UUID.randomUUID());
        assertThat(k.getStatus()).isEqualTo(KycStatus.SUBMITTED);

        k.assignReviewer(UUID.randomUUID());
        assertThat(k.getStatus()).isEqualTo(KycStatus.UNDER_REVIEW);

        k.approve(UUID.randomUUID());
        assertThat(k.getStatus()).isEqualTo(KycStatus.APPROVED);
        assertThat(k.getRaisedEvents()).anyMatch(e -> e instanceof KycApproved);
    }

    @Test
    void cannotApproveBeforeReview() {
        KycRequest k = KycRequest.openOnboarding(UUID.randomUUID());
        assertThatThrownBy(() -> k.approve(UUID.randomUUID()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("UNDER_REVIEW");
    }
}
