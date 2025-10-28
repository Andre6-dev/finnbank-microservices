package com.finnova.customer_service.service.impl;

import com.finnova.customer_service.event.model.CustomerCreatedEvent;
import com.finnova.customer_service.event.model.CustomerDeletedEvent;
import com.finnova.customer_service.event.model.CustomerUpdatedEvent;
import com.finnova.customer_service.event.publisher.CustomerEventPublisher;
import com.finnova.customer_service.exception.CustomerAlreadyExistsException;
import com.finnova.customer_service.exception.CustomerNotFoundException;
import com.finnova.customer_service.exception.InvalidCustomerDataException;
import com.finnova.customer_service.mapper.CustomerMapper;
import com.finnova.customer_service.model.dto.CreateCustomerRequest;
import com.finnova.customer_service.model.dto.CustomerDto;
import com.finnova.customer_service.model.dto.UpdateCustomerRequest;
import com.finnova.customer_service.model.entity.Customer;
import com.finnova.customer_service.model.enums.CustomerType;
import com.finnova.customer_service.repository.CustomerRepository;
import com.finnova.customer_service.service.CustomerCacheService;
import com.finnova.customer_service.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerCacheService cacheService;
    private final CustomerEventPublisher eventPublisher;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               CustomerMapper customerMapper,
                               CustomerCacheService cacheService,
                               CustomerEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.cacheService = cacheService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<CustomerDto> createCustomer(CreateCustomerRequest request) {
        log.info("Creating customer with document: {}", request.getDocumentNumber());

        return validateCreateRequest(request)
                .then(customerRepository.existsByDocumentNumber(request.getDocumentNumber()))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new CustomerAlreadyExistsException(
                                "Customer already exists with document: " + request.getDocumentNumber()));
                    }
                    return customerRepository.existsByEmail(request.getEmail());
                })
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new CustomerAlreadyExistsException(
                                "Customer already exists with email: " + request.getEmail()));
                    }

                    Customer customer = customerMapper.toEntity(request);
                    customer.setActive(true);
                    customer.setCreatedAt(LocalDateTime.now());
                    customer.setUpdatedAt(LocalDateTime.now());

                    return customerRepository.save(customer);
                })
                .flatMap(savedCustomer -> {
                    // Cache the customer
                    return cacheService.cacheCustomer(savedCustomer)
                            .then(Mono.just(savedCustomer));
                })
                .flatMap(savedCustomer -> {
                    // Publish event
                    CustomerCreatedEvent event = CustomerCreatedEvent.builder()
                            .customerId(savedCustomer.getId())
                            .documentNumber(savedCustomer.getDocumentNumber())
                            .customerType(savedCustomer.getCustomerType())
                            .profileType(savedCustomer.getProfileType())
                            .email(savedCustomer.getEmail())
                            .timestamp(LocalDateTime.now())
                            .build();

                    return eventPublisher.publishCustomerCreatedEvent(event)
                            .then(Mono.just(savedCustomer));
                })
                .map(customerMapper::toDto)
                .doOnSuccess(dto -> log.info("Customer created successfully: {}", dto.getId()))
                .doOnError(e -> log.error("Error creating customer: {}", e.getMessage()));
    }

    @Override
    public Mono<CustomerDto> updateCustomer(String id, UpdateCustomerRequest request) {
        log.info("Updating customer: {}", id);

        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(
                        "Customer not found with ID: " + id)))
                .flatMap(existingCustomer -> {
                    if (request.getProfileType() != null) {
                        existingCustomer.setProfileType(request.getProfileType());
                    }
                    if (request.getEmail() != null) {
                        existingCustomer.setEmail(request.getEmail());
                    }
                    if (request.getPhone() != null) {
                        existingCustomer.setPhone(request.getPhone());
                    }
                    if (request.getAddress() != null) {
                        existingCustomer.setAddress(request.getAddress());
                    }
                    if (request.getActive() != null) {
                        existingCustomer.setActive(request.getActive());
                    }
                    existingCustomer.setUpdatedAt(LocalDateTime.now());

                    return customerRepository.save(existingCustomer);
                })
                .flatMap(updatedCustomer -> {
                    // Update cache
                    return cacheService.cacheCustomer(updatedCustomer)
                            .then(Mono.just(updatedCustomer));
                })
                .flatMap(updatedCustomer -> {
                    // Publish event
                    CustomerUpdatedEvent event = CustomerUpdatedEvent.builder()
                            .customerId(updatedCustomer.getId())
                            .documentNumber(updatedCustomer.getDocumentNumber())
                            .profileType(updatedCustomer.getProfileType())
                            .email(updatedCustomer.getEmail())
                            .active(updatedCustomer.getActive())
                            .timestamp(LocalDateTime.now())
                            .build();

                    return eventPublisher.publishCustomerUpdatedEvent(event)
                            .then(Mono.just(updatedCustomer));
                })
                .map(customerMapper::toDto)
                .doOnSuccess(dto -> log.info("Customer updated successfully: {}", id))
                .doOnError(e -> log.error("Error updating customer: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> deleteCustomer(String id) {
        log.info("Deleting customer: {}", id);

        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(
                        "Customer not found with ID: " + id)))
                .flatMap(customer -> {
                    // Evict from cache
                    return cacheService.evictFromCache(id)
                            .then(customerRepository.delete(customer))
                            .then(Mono.just(customer));
                })
                .flatMap(customer -> {
                    // Publish event
                    CustomerDeletedEvent event = CustomerDeletedEvent.builder()
                            .customerId(customer.getId())
                            .documentNumber(customer.getDocumentNumber())
                            .timestamp(LocalDateTime.now())
                            .build();

                    return eventPublisher.publishCustomerDeletedEvent(event);
                })
                .doOnSuccess(v -> log.info("Customer deleted successfully: {}", id))
                .doOnError(e -> log.error("Error deleting customer: {}", e.getMessage()));
    }

    @Override
    public Mono<CustomerDto> findById(String id) {
        log.debug("Finding customer by ID: {}", id);

        // Try to get from cache first
        return cacheService.getFromCache(id)
                .switchIfEmpty(
                        // If not in cache, get from database
                        customerRepository.findById(id)
                                .flatMap(customer ->
                                        // Cache it for next time
                                        cacheService.cacheCustomer(customer)
                                                .then(Mono.just(customer))
                                )
                )
                .map(customerMapper::toDto)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(
                        "Customer not found with ID: " + id)));
    }

    @Override
    public Flux<CustomerDto> findAll() {
        log.debug("Finding all customers");
        return customerRepository.findAll()
                .map(customerMapper::toDto);
    }

    @Override
    public Mono<CustomerDto> findByDocumentNumber(String documentNumber) {
        log.debug("Finding customer by document number: {}", documentNumber);
        return customerRepository.findByDocumentNumber(documentNumber)
                .map(customerMapper::toDto)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(
                        "Customer not found with document number: " + documentNumber)));
    }

    @Override
    public Flux<CustomerDto> findByCustomerType(CustomerType customerType) {
        log.debug("Finding customers by type: {}", customerType);
        return customerRepository.findByCustomerType(customerType)
                .map(customerMapper::toDto);
    }

    @Override
    public Mono<Boolean> validateCustomer(String id) {
        log.debug("Validating customer: {}", id);
        return customerRepository.findById(id)
                .map(customer -> Boolean.TRUE.equals(customer.getActive()))
                .defaultIfEmpty(false);
    }

    /**
     * Validates create customer request based on business rules.
     *
     * @param request the create customer request
     * @return Mono of Void
     */
    private Mono<Void> validateCreateRequest(CreateCustomerRequest request) {
        // Validate personal customer
        if (request.getCustomerType() == CustomerType.PERSONAL) {
            if (request.getFirstName() == null || request.getFirstName().isBlank()) {
                return Mono.error(new InvalidCustomerDataException(
                        "First name is required for personal customers"));
            }
            if (request.getLastName() == null || request.getLastName().isBlank()) {
                return Mono.error(new InvalidCustomerDataException(
                        "Last name is required for personal customers"));
            }
        }

        // Validate business customer
        if (request.getCustomerType() == CustomerType.BUSINESS) {
            if (request.getBusinessName() == null || request.getBusinessName().isBlank()) {
                return Mono.error(new InvalidCustomerDataException(
                        "Business name is required for business customers"));
            }
        }

        return Mono.empty();
    }
}
