package com.santimpay.posctl.identity.web;

import com.santimpay.posctl.identity.application.IdentityService;
import com.santimpay.posctl.identity.application.MeView;
import com.santimpay.posctl.identity.domain.Employee;
import com.santimpay.posctl.identity.domain.User;
import com.santimpay.posctl.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Identity")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class IdentityController {

    private final IdentityService service;

    public record UserResponse(UUID id, String email, String fullName, String status) {}
    public record EmployeeResponse(UUID id, UUID userId, String employeeNo, String department,
                                   String jobTitle, String region, String status) {}

    @Operation(summary = "Current authenticated principal + effective permissions")
    @GetMapping("/me")
    public MeView me() {
        return service.me();
    }

    @Operation(summary = "List users (admin)")
    @GetMapping("/users")
    public PageResponse<UserResponse> users(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {
        Page<User> result = service.users(q, PageRequest.of(page, Math.min(limit, 200)));
        return PageResponse.from(result, result.map(
                u -> new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getStatus()))
                .getContent());
    }

    @Operation(summary = "List employees (admin)")
    @GetMapping("/employees")
    public PageResponse<EmployeeResponse> employees(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {
        Page<Employee> result = service.employees(region, PageRequest.of(page, Math.min(limit, 200)));
        return PageResponse.from(result, result.map(
                e -> new EmployeeResponse(e.getId(), e.getUserId(), e.getEmployeeNo(),
                        e.getDepartment(), e.getJobTitle(), e.getRegion(), e.getStatus()))
                .getContent());
    }
}
