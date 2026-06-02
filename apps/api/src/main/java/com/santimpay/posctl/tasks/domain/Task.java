package com.santimpay.posctl.tasks.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import com.santimpay.posctl.tasks.events.TaskAssigned;
import com.santimpay.posctl.tasks.events.TaskCompleted;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

/**
 * Task aggregate — an assignable unit of work. Can be created by a human, a workflow, AI, or an event
 * reaction ({@link TaskSource}). A polymorphic {@code relatedType}/{@code relatedId} links it to the
 * subject it concerns (a device, merchant, deployment, …) without a cross-module FK.
 */
@Getter
@Entity
@Table(name = "tasks", schema = "tasks")
public class Task extends AggregateRoot<Task> {

    private static final Map<TaskStatus, EnumSet<TaskStatus>> TRANSITIONS = Map.of(
            TaskStatus.OPEN, EnumSet.of(TaskStatus.ASSIGNED, TaskStatus.CANCELLED),
            TaskStatus.ASSIGNED, EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED),
            TaskStatus.IN_PROGRESS, EnumSet.of(TaskStatus.BLOCKED, TaskStatus.DONE, TaskStatus.CANCELLED),
            TaskStatus.BLOCKED, EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED));

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "task_type")
    private String taskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private TaskPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status;

    @Column(name = "assignee_id")
    private UUID assigneeId;

    @Column(name = "related_type")
    private String relatedType;

    @Column(name = "related_id")
    private UUID relatedId;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private TaskSource source;

    protected Task() {}

    public static Task create(String title, String description, String taskType, TaskPriority priority,
                              String relatedType, UUID relatedId, Instant dueAt, TaskSource source) {
        if (title == null || title.isBlank()) {
            throw DomainException.invalidState("title is required");
        }
        Task t = new Task();
        t.assignIdentityIfAbsent();
        t.title = title;
        t.description = description;
        t.taskType = taskType;
        t.priority = priority == null ? TaskPriority.MEDIUM : priority;
        t.status = TaskStatus.OPEN;
        t.relatedType = relatedType;
        t.relatedId = relatedId;
        t.dueAt = dueAt;
        t.source = source == null ? TaskSource.MANUAL : source;
        return t;
    }

    public void assign(UUID assigneeId) {
        if (assigneeId == null) {
            throw DomainException.invalidState("assigneeId is required");
        }
        transition(TaskStatus.ASSIGNED);
        this.assigneeId = assigneeId;
        raise(new TaskAssigned(getId(), assigneeId, title, Instant.now()));
    }

    public void start() { transition(TaskStatus.IN_PROGRESS); }

    public void block() { transition(TaskStatus.BLOCKED); }

    public void complete() {
        transition(TaskStatus.DONE);
        this.completedAt = Instant.now();
        raise(new TaskCompleted(getId(), assigneeId, Instant.now()));
    }

    public void cancel() { transition(TaskStatus.CANCELLED); }

    private void transition(TaskStatus target) {
        EnumSet<TaskStatus> allowed = TRANSITIONS.getOrDefault(status, EnumSet.noneOf(TaskStatus.class));
        if (!allowed.contains(target)) {
            throw DomainException.conflict("Illegal task transition %s -> %s".formatted(status, target));
        }
        this.status = target;
    }
}
