package com.santimpay.posctl.inventory.application;

import com.santimpay.posctl.inventory.domain.DeviceStatus;
import com.santimpay.posctl.inventory.domain.PosDevice;
import com.santimpay.posctl.shared.domain.DomainException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use cases for the inventory context. Mirrors the merchant reference shape. */
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository repository;

    @Transactional
    @PreAuthorize("hasAuthority('PERM_device:create')")
    public PosDevice receive(ReceiveDeviceCommand cmd) {
        if (repository.existsBySerialNo(cmd.serialNo())) {
            throw DomainException.conflict("serialNo already exists: " + cmd.serialNo());
        }
        return repository.save(PosDevice.receiveIntoStock(
                cmd.serialNo(), cmd.model(), cmd.vendor(), cmd.terminalId(), cmd.imei()));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_device:read')")
    public PosDevice get(UUID id) {
        return repository.findById(id).orElseThrow(() -> DomainException.notFound("Device", id));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_device:read')")
    public Page<PosDevice> search(String q, DeviceStatus status, Pageable pageable) {
        return repository.search(q, status, pageable);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_device:update')")
    public PosDevice markFaulty(UUID id, String reason) {
        PosDevice d = get(id);
        d.markFaulty(reason);
        return repository.save(d);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_device:retire')")
    public PosDevice retire(UUID id) {
        PosDevice d = get(id);
        d.retire();
        return repository.save(d);
    }
}
