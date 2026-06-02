package com.santimpay.posctl.tasks.application;

import com.santimpay.posctl.tasks.domain.Task;
import com.santimpay.posctl.tasks.domain.TaskStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskRepository {

    Task save(Task task);

    Optional<Task> findById(UUID id);

    Page<Task> search(TaskStatus status, UUID assigneeId, String taskType, Pageable pageable);
}
