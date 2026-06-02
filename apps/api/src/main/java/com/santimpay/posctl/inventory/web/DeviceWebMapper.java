package com.santimpay.posctl.inventory.web;

import com.santimpay.posctl.inventory.domain.PosDevice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceWebMapper {

    @Mapping(target = "createdAt", source = "audit.createdAt")
    @Mapping(target = "updatedAt", source = "audit.updatedAt")
    @Mapping(target = "version", source = "version")
    DeviceResponse toResponse(PosDevice device);
}
