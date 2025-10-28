package com.finnova.customer_service.mapper;

import com.finnova.customer_service.model.dto.CreateCustomerRequest;
import com.finnova.customer_service.model.dto.CustomerDto;
import com.finnova.customer_service.model.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerMapper {

    /**
     * Converts Customer entity to CustomerDto.
     *
     * @param customer the customer entity
     * @return the customer DTO
     */
    CustomerDto toDto(Customer customer);

    /**
     * Converts CreateCustomerRequest to Customer entity.
     *
     * @param request the create customer request
     * @return the customer entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CreateCustomerRequest request);

    /**
     * Updates a customer entity from DTO.
     *
     * @param customerDto the customer DTO
     * @param customer the customer entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documentType", ignore = true)
    @Mapping(target = "documentNumber", ignore = true)
    @Mapping(target = "customerType", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "businessName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(CustomerDto customerDto, @MappingTarget Customer customer);
}
