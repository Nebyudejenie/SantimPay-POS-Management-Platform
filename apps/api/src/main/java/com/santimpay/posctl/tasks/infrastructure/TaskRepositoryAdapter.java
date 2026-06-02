package com.santimpay.posctl.tasks.infrastructure;

import com.santimpay.posctl.tasks.application.TaskRepository;
import com.santimpay.posctl.tasks.domain.Task;
import com.santimpay.posctl.tasks.domain.TaskStatus;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class TaskRepositoryAdapter implements TaskRepository {

    private final TaskJpaRepository jpa;

    @Override
    public Task save(Task task) {
        return jpa.save(task);
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return jpa.findById(id).filter(t -> !t.getAudit().isDeleted());
    }

    @Override
    public Page<Task> search(TaskStatus status, UUID assigneeId, String taskType, Pageable pageable) {
        return jpa.search(status, assigneeId, taskType, pageable);
    }
}
