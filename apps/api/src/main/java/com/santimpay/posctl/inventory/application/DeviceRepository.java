package com.santimpay.posctl.inventory.application;

import com.santimpay.posctl.inventory.domain.DeviceStatus;
import com.santimpay.posctl.inventory.domain.PosDevice;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Persistence port for the {@link PosDevice} aggregate. */
public interface DeviceRepository {

    PosDevice save(PosDevice device);

    Optional<PosDevice> findById(UUID id);

    boolean existsBySerialNo(String serialNo);

    Page<PosDevice> search(String query, DeviceStatus status, Pageable pageable);
}
