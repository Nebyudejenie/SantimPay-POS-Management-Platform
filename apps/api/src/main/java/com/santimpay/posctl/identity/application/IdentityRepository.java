package com.santimpay.posctl.identity.application;

import com.santimpay.posctl.identity.domain.Employee;
import com.santimpay.posctl.identity.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IdentityRepository {

    User save(User user);

    Optional<User> findUserById(UUID id);

    Optional<User> findUserByKeycloakSub(String sub);

    Page<User> searchUsers(String query, Pageable pageable);

    Employee save(Employee employee);

    Page<Employee> searchEmployees(String region, Pageable pageable);
}
