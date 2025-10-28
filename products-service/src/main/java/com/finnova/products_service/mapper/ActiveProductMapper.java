package com.finnova.products_service.mapper;

import com.finnova.products_service.model.dto.ActiveProductDto;
import com.finnova.products_service.model.dto.CreateActiveProductRequest;
import com.finnova.products_service.model.entity.ActiveProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ActiveProductMapper {

    /**
     * Converts ActiveProduct entity to ActiveProductDto.
     *
     * @param activeProduct the active product entity
     * @return the active product DTO
     */
    ActiveProductDto toDto(ActiveProduct activeProduct);

    /**
     * Converts CreateActiveProductRequest to ActiveProduct entity.
     *
     * @param request the create active product request
     * @return the active product entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creditNumber", ignore = true)
    @Mapping(target = "availableCredit", ignore = true)
    @Mapping(target = "usedCredit", ignore = true)
    @Mapping(target = "minimumPayment", ignore = true)
    @Mapping(target = "outstandingBalance", ignore = true)
    @Mapping(target = "overdueAmount", ignore = true)
    @Mapping(target = "hasOverdueDebt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ActiveProduct toEntity(CreateActiveProductRequest request);

    /**
     * Updates an active product entity from DTO.
     *
     * @param activeProductDto the active product DTO
     * @param activeProduct the active product entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creditNumber", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "productType", ignore = true)
    @Mapping(target = "availableCredit", ignore = true)
    @Mapping(target = "usedCredit", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "minimumPayment", ignore = true)
    @Mapping(target = "outstandingBalance", ignore = true)
    @Mapping(target = "overdueAmount", ignore = true)
    @Mapping(target = "hasOverdueDebt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(ActiveProductDto activeProductDto, @MappingTarget ActiveProduct activeProduct);
}
