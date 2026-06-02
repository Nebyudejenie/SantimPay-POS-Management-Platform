package com.santimpay.posctl.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.santimpay.posctl.shared.domain.DomainException;
import com.santimpay.posctl.tasks.domain.Task;
import com.santimpay.posctl.tasks.domain.TaskPriority;
import com.santimpay.posctl.tasks.domain.TaskSource;
import com.santimpay.posctl.tasks.domain.TaskStatus;
import com.santimpay.posctl.tasks.events.TaskAssigned;
import com.santimpay.posctl.tasks.events.TaskCompleted;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TaskAggregateTest {

    @Test
    void create_assign_start_complete_happyPath() {
        Task t = Task.create("Swap device", "faulty", "device_swap", TaskPriority.HIGH,
                "device", UUID.randomUUID(), null, TaskSource.SYSTEM);
        assertThat(t.getStatus()).isEqualTo(TaskStatus.OPEN);

        UUID assignee = UUID.randomUUID();
        t.assign(assignee);
        assertThat(t.getStatus()).isEqualTo(TaskStatus.ASSIGNED);
        assertThat(t.getRaisedEvents()).anyMatch(e -> e instanceof TaskAssigned);

        t.start();
        t.complete();
        assertThat(t.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(t.getCompletedAt()).isNotNull();
        assertThat(t.getRaisedEvents()).anyMatch(e -> e instanceof TaskCompleted);
    }

    @Test
    void cannotCompleteAnOpenTask() {
        Task t = Task.create("x", null, null, null, null, null, null, TaskSource.MANUAL);
        assertThatThrownBy(t::complete)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Illegal task transition");
    }
}
