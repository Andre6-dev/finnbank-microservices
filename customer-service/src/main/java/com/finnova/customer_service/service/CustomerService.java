package com.finnova.customer_service.service;

import com.finnova.customer_service.model.dto.CreateCustomerRequest;
import com.finnova.customer_service.model.dto.CustomerDto;
import com.finnova.customer_service.model.dto.UpdateCustomerRequest;
import com.finnova.customer_service.model.enums.CustomerType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {

    /**
     * Creates a new customer.
     *
     * @param request the create customer request
     * @return Mono of CustomerDto
     */
    Mono<CustomerDto> createCustomer(CreateCustomerRequest request);

    /**
     * Updates a customer.
     *
     * @param id the customer ID
     * @param request the update customer request
     * @return Mono of CustomerDto
     */
    Mono<CustomerDto> updateCustomer(String id, UpdateCustomerRequest request);

    /**
     * Deletes a customer.
     *
     * @param id the customer ID
     * @return Mono of Void
     */
    Mono<Void> deleteCustomer(String id);

    /**
     * Finds a customer by ID.
     *
     * @param id the customer ID
     * @return Mono of CustomerDto
     */
    Mono<CustomerDto> findById(String id);

    /**
     * Finds all customers.
     *
     * @return Flux of CustomerDto
     */
    Flux<CustomerDto> findAll();

    /**
     * Finds a customer by document number.
     *
     * @param documentNumber the document number
     * @return Mono of CustomerDto
     */
    Mono<CustomerDto> findByDocumentNumber(String documentNumber);

    /**
     * Finds customers by customer type.
     *
     * @param customerType the customer type
     * @return Flux of CustomerDto
     */
    Flux<CustomerDto> findByCustomerType(CustomerType customerType);

    /**
     * Validates a customer.
     *
     * @param id the customer ID
     * @return Mono of Boolean - true if valid and active, false otherwise
     */
    Mono<Boolean> validateCustomer(String id);
}
