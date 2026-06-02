package com.santimpay.posctl.identity.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;

/** HR/org extension of a user — department, role, region (used for region-scoped RBAC). */
@Getter
@Entity
@Table(name = "employees", schema = "identity")
public class Employee extends AggregateRoot<Employee> {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "employee_no", nullable = false, unique = true)
    private String employeeNo;

    @Column(name = "department")
    private String department;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "region")
    private String region;

    @Column(name = "manager_id")
    private UUID managerId;

    @Column(name = "hired_at")
    private LocalDate hiredAt;

    @Column(name = "status", nullable = false)
    private String status;

    protected Employee() {}

    public static Employee create(UUID userId, String employeeNo, String department,
                                  String jobTitle, String region) {
        Employee e = new Employee();
        e.assignIdentityIfAbsent();
        e.userId = userId;
        e.employeeNo = employeeNo;
        e.department = department;
        e.jobTitle = jobTitle;
        e.region = region;
        e.status = "active";
        return e;
    }
}
