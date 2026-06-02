package com.santimpay.posctl.identity.infrastructure;

import com.santimpay.posctl.identity.domain.Employee;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface EmployeeJpaRepository extends JpaRepository<Employee, UUID> {

    @Query("""
           select e from Employee e
           where e.audit.deletedAt is null
             and (:region is null or e.region = :region)
           """)
    Page<Employee> search(@Param("region") String region, Pageable pageable);
}
