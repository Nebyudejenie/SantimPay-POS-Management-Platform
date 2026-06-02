package com.santimpay.posctl.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.santimpay.posctl.deployment.domain.Deployment;
import com.santimpay.posctl.deployment.domain.DeploymentStatus;
import com.santimpay.posctl.deployment.domain.DeviceAssignment;
import com.santimpay.posctl.deployment.events.DeploymentCompleted;
import com.santimpay.posctl.deployment.events.DeviceAssigned;
import com.santimpay.posctl.shared.domain.DomainException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeploymentAggregateTest {

    @Test
    void complete_bindsDevice_andRaisesEvent() {
        Deployment d = Deployment.plan("D-1", LocalDate.now(), UUID.randomUUID(), UUID.randomUUID(), null);
        UUID device = UUID.randomUUID();

        d.complete(device, "Abebe", 9.01, 38.74, "delivered", "trello-1");

        assertThat(d.getStatus()).isEqualTo(DeploymentStatus.COMPLETED);
        assertThat(d.getDeviceId()).isEqualTo(device);
        assertThat(d.getRaisedEvents()).anyMatch(e -> e instanceof DeploymentCompleted);
    }

    @Test
    void complete_withoutDevice_isRejected() {
        Deployment d = Deployment.plan("D-2", LocalDate.now(), UUID.randomUUID(), UUID.randomUUID(), null);
        assertThatThrownBy(() -> d.complete(null, "x", null, null, null, null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void openingAssignment_raisesDeviceAssigned_andClosingEndsPeriod() {
        DeviceAssignment a = DeviceAssignment.open(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        assertThat(a.isCurrent()).isTrue();
        assertThat(a.getRaisedEvents()).anyMatch(e -> e instanceof DeviceAssigned);

        a.close();
        assertThat(a.isCurrent()).isFalse();
        assertThat(a.getValidTo()).isNotNull();
    }
}
