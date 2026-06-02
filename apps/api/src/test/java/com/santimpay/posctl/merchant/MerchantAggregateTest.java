package com.santimpay.posctl.merchant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.santimpay.posctl.merchant.domain.Merchant;
import com.santimpay.posctl.merchant.domain.MerchantStatus;
import com.santimpay.posctl.merchant.events.MerchantActivated;
import com.santimpay.posctl.merchant.events.MerchantOnboarded;
import com.santimpay.posctl.shared.domain.DomainException;
import org.junit.jupiter.api.Test;

/** Pure domain unit tests — no Spring context. Fast feedback on the invariants. */
class MerchantAggregateTest {

    @Test
    void onboard_createsOnboardingMerchant_andRaisesEvent() {
        Merchant m = Merchant.onboard("M-001", "Acme Trading PLC", "Acme", "TIN123", "retail");

        assertThat(m.getId()).isNotNull();
        assertThat(m.getStatus()).isEqualTo(MerchantStatus.ONBOARDING);
        assertThat(m.getRaisedEvents()).anyMatch(e -> e instanceof MerchantOnboarded);
    }

    @Test
    void activate_fromOnboarding_succeeds_andRaisesEvent() {
        Merchant m = Merchant.onboard("M-002", "Beta Trading", null, null, null);
        m.activate();

        assertThat(m.getStatus()).isEqualTo(MerchantStatus.ACTIVE);
        assertThat(m.getActivatedAt()).isNotNull();
        assertThat(m.getRaisedEvents()).anyMatch(e -> e instanceof MerchantActivated);
    }

    @Test
    void activate_whenAlreadyActive_isRejected() {
        Merchant m = Merchant.onboard("M-003", "Gamma", null, null, null);
        m.activate();

        assertThatThrownBy(m::activate)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("cannot be activated");
    }

    @Test
    void onboard_withoutLegalName_isRejected() {
        assertThatThrownBy(() -> Merchant.onboard("M-004", "  ", null, null, null))
                .isInstanceOf(DomainException.class);
    }
}
