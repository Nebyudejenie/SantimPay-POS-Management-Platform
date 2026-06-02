package com.santimpay.posctl.tasks.application;

import com.santimpay.posctl.shared.domain.DomainException;
import com.santimpay.posctl.tasks.domain.Task;
import com.santimpay.posctl.tasks.domain.TaskPriority;
import com.santimpay.posctl.tasks.domain.TaskSource;
import com.santimpay.posctl.tasks.domain.TaskStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Task use cases. Implements {@link TaskCreation} (system/event-driven, no auth) plus the human-facing
 * CRUD/transition use cases (permission-guarded).
 */
@Service
@RequiredArgsConstructor
public class TaskService implements TaskCreation {

    private final TaskRepository repository;

    @Override
    @Transactional
    public UUID createSystemTask(String title, String description, String taskType,
                                 TaskPriority priority, String relatedType, UUID relatedId) {
        Task task = Task.create(title, description, taskType, priority, relatedType, relatedId,
                null, TaskSource.SYSTEM);
        return repository.save(task).getId();
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_task:create')")
    public Task create(String title, String description, String taskType, TaskPriority priority,
                       String relatedType, UUID relatedId, Instant dueAt) {
        return repository.save(Task.create(title, description, taskType, priority, relatedType,
                relatedId, dueAt, TaskSource.MANUAL));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_task:read')")
    public Task get(UUID id) {
        return repository.findById(id).orElseThrow(() -> DomainException.notFound("Task", id));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_task:read')")
    public Page<Task> search(TaskStatus status, UUID assigneeId, String taskType, Pageable pageable) {
        return repository.search(status, assigneeId, taskType, pageable);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_task:assign')")
    public Task assign(UUID id, UUID assigneeId) {
        Task task = get(id);
        task.assign(assigneeId);
        return repository.save(task);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_task:update')")
    public Task complete(UUID id) {
        Task task = get(id);
        task.complete();
        return repository.save(task);
    }
}
