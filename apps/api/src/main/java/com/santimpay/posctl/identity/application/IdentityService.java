package com.santimpay.posctl.identity.application;

import com.santimpay.posctl.identity.domain.Employee;
import com.santimpay.posctl.identity.domain.User;
import com.santimpay.posctl.shared.security.CurrentUser;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Identity read/admin use cases. {@code me()} assembles the current principal from the validated JWT
 * (roles/permissions are claims) — no DB hit required for the common path. User/employee management
 * is permission-guarded ({@code user:manage}).
 */
@Service
@RequiredArgsConstructor
public class IdentityService {

    private final IdentityRepository repository;

    @Transactional(readOnly = true)
    public MeView me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .collect(Collectors.toSet());
        UUID id = CurrentUser.id().orElse(null);
        String email = auth.getName();
        return new MeView(id, email, email, roles, CurrentUser.permissions());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_user:manage')")
    public Page<User> users(String query, Pageable pageable) {
        return repository.searchUsers(query, pageable);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_user:manage')")
    public Page<Employee> employees(String region, Pageable pageable) {
        return repository.searchEmployees(region, pageable);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_user:manage')")
    public Employee createEmployee(UUID userId, String employeeNo, String department,
                                   String jobTitle, String region) {
        return repository.save(Employee.create(userId, employeeNo, department, jobTitle, region));
    }
}
