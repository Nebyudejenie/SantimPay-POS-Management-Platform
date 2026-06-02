package com.santimpay.posctl.inventory.web;

import com.santimpay.posctl.inventory.application.DeviceService;
import com.santimpay.posctl.inventory.application.ReceiveDeviceCommand;
import com.santimpay.posctl.inventory.domain.DeviceStatus;
import com.santimpay.posctl.inventory.domain.PosDevice;
import com.santimpay.posctl.inventory.web.DeviceRequests.MarkFaultyRequest;
import com.santimpay.posctl.inventory.web.DeviceRequests.ReceiveDeviceRequest;
import com.santimpay.posctl.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Devices")
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService service;
    private final DeviceWebMapper mapper;

    @Operation(summary = "Receive a device into stock")
    @PostMapping
    public ResponseEntity<DeviceResponse> receive(@Valid @RequestBody ReceiveDeviceRequest req) {
        PosDevice d = service.receive(new ReceiveDeviceCommand(
                req.serialNo(), req.model(), req.vendor(), req.terminalId(), req.imei()));
        return ResponseEntity.created(URI.create("/api/v1/devices/" + d.getId()))
                .body(mapper.toResponse(d));
    }

    @Operation(summary = "Get a device by id")
    @GetMapping("/{id}")
    public DeviceResponse get(@PathVariable UUID id) {
        return mapper.toResponse(service.get(id));
    }

    @Operation(summary = "Search / list devices")
    @GetMapping
    public PageResponse<DeviceResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) DeviceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {
        Page<PosDevice> result = service.search(q, status,
                PageRequest.of(page, Math.min(limit, 200), Sort.by(Sort.Direction.DESC, "id")));
        return PageResponse.from(result, result.map(mapper::toResponse).getContent());
    }

    @Operation(summary = "Mark a device faulty (raises DeviceMarkedFaulty)")
    @PostMapping("/{id}:mark-faulty")
    public DeviceResponse markFaulty(@PathVariable UUID id, @Valid @RequestBody MarkFaultyRequest req) {
        return mapper.toResponse(service.markFaulty(id, req.reason()));
    }

    @Operation(summary = "Retire a device")
    @PostMapping("/{id}:retire")
    public DeviceResponse retire(@PathVariable UUID id) {
        return mapper.toResponse(service.retire(id));
    }
}
