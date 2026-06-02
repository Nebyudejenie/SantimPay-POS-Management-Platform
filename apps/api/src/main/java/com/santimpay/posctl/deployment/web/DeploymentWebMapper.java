package com.santimpay.posctl.deployment.web;

import com.santimpay.posctl.deployment.domain.Deployment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeploymentWebMapper {

    @Mapping(target = "createdAt", source = "audit.createdAt")
    @Mapping(target = "updatedAt", source = "audit.updatedAt")
    @Mapping(target = "version", source = "audit.version")
    DeploymentResponse toResponse(Deployment deployment);
}
