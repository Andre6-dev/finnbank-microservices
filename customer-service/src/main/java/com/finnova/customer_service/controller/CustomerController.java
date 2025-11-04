package com.finnova.customer_service.controller;

import com.finnova.customer_service.model.dto.CreateCustomerRequest;
import com.finnova.customer_service.model.dto.CustomerDto;
import com.finnova.customer_service.model.dto.CustomerValidationResponse;
import com.finnova.customer_service.model.dto.UpdateCustomerRequest;
import com.finnova.customer_service.model.enums.CustomerType;
import com.finnova.customer_service.service.CustomerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/customers")
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Creates a new customer.
     *
     * @param request the create customer request
     * @return Mono of ResponseEntity with CustomerDto
     */
    @PostMapping
    public Mono<ResponseEntity<CustomerDto>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        log.info("POST /customers - Creating customer with document: {}",
                request.getDocumentNumber());
        return customerService.createCustomer(request)
                .map(customer -> ResponseEntity.status(HttpStatus.CREATED).body(customer))
                .doOnSuccess(r -> log.info("Customer created successfully"))
                .doOnError(e -> log.error("Error creating customer: {}", e.getMessage()));
    }

    /**
     * Gets all customers.
     *
     * @return Flux of CustomerDto
     */
    @GetMapping
    public Flux<CustomerDto> getAllCustomers() {
        log.info("GET /customers - Getting all customers");
        return customerService.findAll()
                .doOnComplete(() -> log.info("Retrieved all customers"));
    }

    /**
     * Gets a customer by ID.
     *
     * @param id the customer ID
     * @return Mono of ResponseEntity with CustomerDto
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CustomerDto>> getCustomerById(@PathVariable String id) {
        log.info("GET /customers/{} - Getting customer by ID", id);
        return customerService.findById(id)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Customer retrieved successfully: {}", id))
                .doOnError(e -> log.error("Error retrieving customer: {}", e.getMessage()));
    }

    /**
     * Gets a customer by document number.
     *
     * @param documentNumber the document number
     * @return Mono of ResponseEntity with CustomerDto
     */
    @GetMapping("/document/{documentNumber}")
    public Mono<ResponseEntity<CustomerDto>> getCustomerByDocumentNumber(
            @PathVariable String documentNumber) {
        log.info("GET /customers/document/{} - Getting customer by document", documentNumber);
        return customerService.findByDocumentNumber(documentNumber)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Customer retrieved by document: {}", documentNumber))
                .doOnError(e -> log.error("Error retrieving customer: {}", e.getMessage()));
    }

    /**
     * Gets customers by customer type.
     *
     * @param type the customer type
     * @return Flux of CustomerDto
     */
    @GetMapping("/type/{type}")
    public Flux<CustomerDto> getCustomersByType(@PathVariable CustomerType type) {
        log.info("GET /customers/type/{} - Getting customers by type", type);
        return customerService.findByCustomerType(type)
                .doOnComplete(() -> log.info("Retrieved customers by type: {}", type));
    }

    /**
     * Updates a customer.
     *
     * @param id the customer ID
     * @param request the update customer request
     * @return Mono of ResponseEntity with CustomerDto
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<CustomerDto>> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        log.info("PUT /customers/{} - Updating customer", id);
        return customerService.updateCustomer(id, request)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Customer updated successfully: {}", id))
                .doOnError(e -> log.error("Error updating customer: {}", e.getMessage()));
    }

    /**
     * Deletes a customer.
     *
     * @param id the customer ID
     * @return Mono of ResponseEntity
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteCustomer(@PathVariable String id) {
        log.info("DELETE /customers/{} - Deleting customer", id);
        return customerService.deleteCustomer(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .doOnSuccess(r -> log.info("Customer deleted successfully: {}", id))
                .doOnError(e -> log.error("Error deleting customer: {}", e.getMessage()));
    }

    /**
     * Validates a customer.
     *
     * @param id the customer ID
     * @return Mono of ResponseEntity with validation result
     */
    @GetMapping("/{id}/validate")
    public Mono<ResponseEntity<CustomerValidationResponse>> validateCustomer(@PathVariable String id) {
        log.info("GET /customers/{}/validate - Validating customer", id);
        return customerService.validateCustomer(id)
                .map(isValid -> ResponseEntity.ok(new CustomerValidationResponse(isValid)))
                .doOnSuccess(r -> log.info("Customer validation completed: {}", id))
                .doOnError(e -> log.error("Error validating customer: {}", e.getMessage()));
    }
}
