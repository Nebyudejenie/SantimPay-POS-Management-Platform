package com.santimpay.posctl.identity.infrastructure;

import com.santimpay.posctl.identity.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserJpaRepository extends JpaRepository<User, UUID> {

    Optional<User> findByKeycloakSub(String keycloakSub);

    @Query("""
           select u from User u
           where u.audit.deletedAt is null
             and (:q is null or lower(u.fullName) like lower(concat('%', :q, '%'))
                             or lower(u.email) like lower(concat('%', :q, '%')))
           """)
    Page<User> search(@Param("q") String query, Pageable pageable);
}
