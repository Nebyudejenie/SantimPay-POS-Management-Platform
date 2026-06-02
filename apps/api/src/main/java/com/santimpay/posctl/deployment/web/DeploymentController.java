package com.santimpay.posctl.deployment.web;

import com.santimpay.posctl.deployment.application.CompleteDeploymentCommand;
import com.santimpay.posctl.deployment.application.DeploymentService;
import com.santimpay.posctl.deployment.application.PlanDeploymentCommand;
import com.santimpay.posctl.deployment.domain.Deployment;
import com.santimpay.posctl.deployment.domain.DeploymentStatus;
import com.santimpay.posctl.deployment.web.DeploymentRequests.CompleteDeploymentRequest;
import com.santimpay.posctl.deployment.web.DeploymentRequests.PlanDeploymentRequest;
import com.santimpay.posctl.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Deployments")
@RestController
@RequestMapping("/api/v1/deployments")
@RequiredArgsConstructor
public class DeploymentController {

    private final DeploymentService service;
    private final DeploymentWebMapper mapper;

    @Operation(summary = "Plan a deployment")
    @PostMapping
    public ResponseEntity<DeploymentResponse> plan(@Valid @RequestBody PlanDeploymentRequest req) {
        Deployment d = service.plan(new PlanDeploymentCommand(
                req.deploymentNo(), req.scheduledDate(), req.merchantId(),
                req.branchId(), req.assignedAgent()));
        return ResponseEntity.created(URI.create("/api/v1/deployments/" + d.getId()))
                .body(mapper.toResponse(d));
    }

    @Operation(summary = "Get a deployment")
    @GetMapping("/{id}")
    public DeploymentResponse get(@PathVariable UUID id) {
        return mapper.toResponse(service.get(id));
    }

    @Operation(summary = "List deployments (filter by date/status/agent)")
    @GetMapping
    public PageResponse<DeploymentResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) DeploymentStatus status,
            @RequestParam(required = false) UUID agent,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {
        Page<Deployment> result = service.search(date, status, agent,
                PageRequest.of(page, Math.min(limit, 200), Sort.by(Sort.Direction.DESC, "id")));
        return PageResponse.from(result, result.map(mapper::toResponse).getContent());
    }

    @Operation(summary = "Complete a deployment (binds device, raises DeviceAssigned + DeploymentCompleted)")
    @PostMapping("/{id}:complete")
    public DeploymentResponse complete(@PathVariable UUID id,
                                       @Valid @RequestBody CompleteDeploymentRequest req) {
        return mapper.toResponse(service.complete(new CompleteDeploymentCommand(
                id, req.deviceId(), req.receivedBy(), req.latitude(), req.longitude(),
                req.conversationNotes(), req.trelloCardId())));
    }
}
