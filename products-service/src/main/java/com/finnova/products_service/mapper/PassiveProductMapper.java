package com.finnova.products_service.mapper;

import com.finnova.products_service.model.dto.CreatePassiveProductRequest;
import com.finnova.products_service.model.dto.PassiveProductDto;
import com.finnova.products_service.model.entity.PassiveProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PassiveProductMapper {

    /**
     * Converts PassiveProduct entity to PassiveProductDto.
     *
     * @param passiveProduct the passive product entity
     * @return the passive product DTO
     */
    PassiveProductDto toDto(PassiveProduct passiveProduct);

    /**
     * Converts CreatePassiveProductRequest to PassiveProduct entity.
     *
     * @param request the create passive product request
     * @return the passive product entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "currentMonthTransactions", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PassiveProduct toEntity(CreatePassiveProductRequest request);

    /**
     * Updates a passive product entity from DTO.
     *
     * @param passiveProductDto the passive product DTO
     * @param passiveProduct the passive product entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "productType", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "openingAmount", ignore = true)
    @Mapping(target = "currentMonthTransactions", ignore = true)
    @Mapping(target = "movementDay", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(PassiveProductDto passiveProductDto, @MappingTarget PassiveProduct passiveProduct);
}
