package com.santimpay.posctl.identity.infrastructure;

import com.santimpay.posctl.identity.application.IdentityRepository;
import com.santimpay.posctl.identity.domain.Employee;
import com.santimpay.posctl.identity.domain.User;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class IdentityRepositoryAdapter implements IdentityRepository {

    private final UserJpaRepository userJpa;
    private final EmployeeJpaRepository employeeJpa;

    @Override
    public User save(User user) {
        return userJpa.save(user);
    }

    @Override
    public Optional<User> findUserById(UUID id) {
        return userJpa.findById(id);
    }

    @Override
    public Optional<User> findUserByKeycloakSub(String sub) {
        return userJpa.findByKeycloakSub(sub);
    }

    @Override
    public Page<User> searchUsers(String query, Pageable pageable) {
        return userJpa.search(query, pageable);
    }

    @Override
    public Employee save(Employee employee) {
        return employeeJpa.save(employee);
    }

    @Override
    public Page<Employee> searchEmployees(String region, Pageable pageable) {
        return employeeJpa.search(region, pageable);
    }
}
