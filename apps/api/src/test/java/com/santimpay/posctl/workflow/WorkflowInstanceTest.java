package com.santimpay.posctl.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.santimpay.posctl.shared.domain.DomainException;
import com.santimpay.posctl.workflow.domain.WorkflowInstance;
import com.santimpay.posctl.workflow.domain.WorkflowStatus;
import com.santimpay.posctl.workflow.domain.WorkflowType;
import com.santimpay.posctl.workflow.events.ApprovalGranted;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WorkflowInstanceTest {

    @Test
    void approve_byDifferentUser_grantsAndRaisesEvent() {
        UUID maker = UUID.randomUUID();
        UUID checker = UUID.randomUUID();
        UUID merchant = UUID.randomUUID();
        WorkflowInstance w = WorkflowInstance.initiate(
                WorkflowType.MERCHANT_ACTIVATION, "merchant", merchant, maker);

        w.approve(checker);

        assertThat(w.getStatus()).isEqualTo(WorkflowStatus.APPROVED);
        assertThat(w.getRaisedEvents()).anyMatch(e -> e instanceof ApprovalGranted g
                && g.subjectId().equals(merchant));
    }

    @Test
    void makerCannotBeChecker() {
        UUID maker = UUID.randomUUID();
        WorkflowInstance w = WorkflowInstance.initiate(
                WorkflowType.MERCHANT_ACTIVATION, "merchant", UUID.randomUUID(), maker);

        assertThatThrownBy(() -> w.approve(maker))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Segregation of duties");
    }

    @Test
    void cannotDecideTwice() {
        WorkflowInstance w = WorkflowInstance.initiate(
                WorkflowType.MERCHANT_ACTIVATION, "merchant", UUID.randomUUID(), UUID.randomUUID());
        w.approve(UUID.randomUUID());

        assertThatThrownBy(() -> w.approve(UUID.randomUUID()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("not pending");
    }
}
