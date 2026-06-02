package com.santimpay.posctl.identity.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

/**
 * Local projection of a Keycloak user. Authentication & credentials live in Keycloak (the IdP);
 * this row exists so the platform can reference an internal {@code UserId}, attach employee data, and
 * carry the local active/suspended flag. Provisioned/updated from Keycloak (JIT on first login or via
 * admin sync) — maps to the table created in V1_0001.
 */
@Getter
@Entity
@Table(name = "users", schema = "identity")
public class User extends AggregateRoot<User> {

    @Column(name = "keycloak_sub", unique = true)
    private String keycloakSub;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    protected User() {}

    public static User provision(String keycloakSub, String email, String fullName, String phone) {
        User u = new User();
        u.assignIdentityIfAbsent();
        u.keycloakSub = keycloakSub;
        u.email = email;
        u.fullName = fullName;
        u.phone = phone;
        u.status = "active";
        return u;
    }

    public void recordLogin() {
        this.lastLoginAt = Instant.now();
    }
}
