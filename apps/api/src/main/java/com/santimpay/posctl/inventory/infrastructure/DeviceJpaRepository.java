package com.santimpay.posctl.inventory.infrastructure;

import com.santimpay.posctl.inventory.domain.DeviceStatus;
import com.santimpay.posctl.inventory.domain.PosDevice;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface DeviceJpaRepository extends JpaRepository<PosDevice, UUID> {

    boolean existsBySerialNo(String serialNo);

    @Query("""
           select d from PosDevice d
           where d.audit.deletedAt is null
             and (:status is null or d.status = :status)
             and (:q is null or lower(d.serialNo) like lower(concat('%', :q, '%'))
                             or lower(d.terminalId) like lower(concat('%', :q, '%')))
           """)
    Page<PosDevice> search(@Param("q") String query,
                           @Param("status") DeviceStatus status,
                           Pageable pageable);
}
