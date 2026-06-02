package com.santimpay.posctl.tasks.web;

import com.santimpay.posctl.shared.web.PageResponse;
import com.santimpay.posctl.tasks.application.TaskService;
import com.santimpay.posctl.tasks.domain.Task;
import com.santimpay.posctl.tasks.domain.TaskPriority;
import com.santimpay.posctl.tasks.domain.TaskStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tasks")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService service;

    public record CreateTaskRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 2000) String description,
            @Size(max = 60) String taskType,
            TaskPriority priority,
            @Size(max = 40) String relatedType,
            UUID relatedId,
            Instant dueAt) {}

    public record AssignRequest(@NotNull UUID assigneeId) {}

    public record TaskResponse(UUID id, String title, String description, String taskType,
                               String priority, String status, UUID assigneeId, String relatedType,
                               UUID relatedId, Instant dueAt, Instant completedAt, String source,
                               Instant createdAt) {}

    private TaskResponse toResponse(Task t) {
        return new TaskResponse(t.getId(), t.getTitle(), t.getDescription(), t.getTaskType(),
                t.getPriority().name(), t.getStatus().name(), t.getAssigneeId(), t.getRelatedType(),
                t.getRelatedId(), t.getDueAt(), t.getCompletedAt(), t.getSource().name(),
                t.getAudit().getCreatedAt());
    }

    @Operation(summary = "Create a task")
    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest req) {
        Task t = service.create(req.title(), req.description(), req.taskType(), req.priority(),
                req.relatedType(), req.relatedId(), req.dueAt());
        return ResponseEntity.created(java.net.URI.create("/api/v1/tasks/" + t.getId()))
                .body(toResponse(t));
    }

    @Operation(summary = "Get a task")
    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable UUID id) {
        return toResponse(service.get(id));
    }

    @Operation(summary = "List tasks (filter by status/assignee/type)")
    @GetMapping
    public PageResponse<TaskResponse> list(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) UUID assignee,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {
        Page<Task> result = service.search(status, assignee, type,
                PageRequest.of(page, Math.min(limit, 200), Sort.by(Sort.Direction.DESC, "id")));
        return PageResponse.from(result, result.map(this::toResponse).getContent());
    }

    @Operation(summary = "Assign a task")
    @PostMapping("/{id}:assign")
    public TaskResponse assign(@PathVariable UUID id, @Valid @RequestBody AssignRequest req) {
        return toResponse(service.assign(id, req.assigneeId()));
    }

    @Operation(summary = "Complete a task")
    @PostMapping("/{id}:complete")
    public TaskResponse complete(@PathVariable UUID id) {
        return toResponse(service.complete(id));
    }
}
