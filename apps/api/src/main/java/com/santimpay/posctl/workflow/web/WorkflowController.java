package com.santimpay.posctl.workflow.web;

import com.santimpay.posctl.shared.web.PageResponse;
import com.santimpay.posctl.workflow.application.WorkflowService;
import com.santimpay.posctl.workflow.domain.WorkflowInstance;
import com.santimpay.posctl.workflow.domain.WorkflowStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Workflows")
@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService service;

    public record WorkflowResponse(UUID id, String workflowType, String subjectType, UUID subjectId,
                                   String status, int currentStep, int totalSteps, UUID initiatedBy,
                                   Instant createdAt) {}

    public record DecisionRequest(@Size(max = 1000) String comment) {}

    private WorkflowResponse toResponse(WorkflowInstance w) {
        return new WorkflowResponse(w.getId(), w.getWorkflowType().name(), w.getSubjectType(),
                w.getSubjectId(), w.getStatus().name(), w.getCurrentStep(), w.getTotalSteps(),
                w.getInitiatedBy(), w.getAudit().getCreatedAt());
    }

    @Operation(summary = "Get a workflow instance")
    @GetMapping("/{id}")
    public WorkflowResponse get(@PathVariable UUID id) {
        return toResponse(service.get(id));
    }

    @Operation(summary = "List workflow instances (filter by status/subjectType)")
    @GetMapping
    public PageResponse<WorkflowResponse> list(
            @RequestParam(required = false) WorkflowStatus status,
            @RequestParam(required = false) String subjectType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {
        Page<WorkflowInstance> result = service.search(status, subjectType,
                PageRequest.of(page, Math.min(limit, 200), Sort.by(Sort.Direction.DESC, "id")));
        return PageResponse.from(result, result.map(this::toResponse).getContent());
    }

    @Operation(summary = "Approve a pending workflow (requires workflow:approve; maker≠checker enforced)")
    @PostMapping("/{id}:approve")
    public WorkflowResponse approve(@PathVariable UUID id, @Valid @RequestBody DecisionRequest req) {
        return toResponse(service.approve(id, req.comment()));
    }

    @Operation(summary = "Reject a pending workflow")
    @PostMapping("/{id}:reject")
    public WorkflowResponse reject(@PathVariable UUID id, @Valid @RequestBody DecisionRequest req) {
        return toResponse(service.reject(id, req.comment()));
    }
}
