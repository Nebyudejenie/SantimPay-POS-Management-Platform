package com.santimpay.posctl.inventory.infrastructure;

import com.santimpay.posctl.inventory.application.DeviceRepository;
import com.santimpay.posctl.inventory.domain.DeviceStatus;
import com.santimpay.posctl.inventory.domain.PosDevice;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class DeviceRepositoryAdapter implements DeviceRepository {

    private final DeviceJpaRepository jpa;

    @Override
    public PosDevice save(PosDevice device) {
        return jpa.save(device);
    }

    @Override
    public Optional<PosDevice> findById(UUID id) {
        return jpa.findById(id).filter(d -> !d.getAudit().isDeleted());
    }

    @Override
    public boolean existsBySerialNo(String serialNo) {
        return jpa.existsBySerialNo(serialNo);
    }

    @Override
    public Page<PosDevice> search(String query, DeviceStatus status, Pageable pageable) {
        return jpa.search(query, status, pageable);
    }
}
