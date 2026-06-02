package com.santimpay.posctl.merchant.web;

import com.santimpay.posctl.merchant.domain.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct mapper: domain aggregate -> web response. Generated impl is a Spring bean. */
@Mapper(componentModel = "spring")
public interface MerchantWebMapper {

    @Mapping(target = "createdAt", source = "audit.createdAt")
    @Mapping(target = "updatedAt", source = "audit.updatedAt")
    @Mapping(target = "version", source = "version")
    MerchantResponse toResponse(Merchant merchant);
}
