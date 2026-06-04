package com.santimpay.posctl.merchant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;

/** Reference bank (read-only here; lives in the inventory schema, seeded as reference data). */
@Getter
@Entity
@Table(name = "banks", schema = "inventory")
public class Bank {
    @Id
    @Column(name = "id")
    private UUID id;
    @Column(name = "code")
    private String code;
    @Column(name = "name")
    private String name;
    @Column(name = "swift")
    private String swift;
    @Column(name = "is_active")
    private boolean active;

    protected Bank() {}
}
