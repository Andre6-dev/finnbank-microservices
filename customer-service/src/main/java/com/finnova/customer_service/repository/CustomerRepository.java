package com.finnova.customer_service.repository;

import com.finnova.customer_service.model.entity.Customer;
import com.finnova.customer_service.model.enums.CustomerType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

    /**
     * Finds a customer by document number.
     *
     * @param documentNumber the document number
     * @return Mono of Customer if found, empty Mono otherwise
     */
    Mono<Customer> findByDocumentNumber(String documentNumber);

    /**
     * Finds customers by customer type.
     *
     * @param customerType the customer type
     * @return Flux of Customers
     */
    Flux<Customer> findByCustomerType(CustomerType customerType);

    /**
     * Finds customers by email.
     *
     * @param email the email
     * @return Mono of Customer if found, empty Mono otherwise
     */
    Mono<Customer> findByEmail(String email);

    /**
     * Checks if a customer exists with the given document number.
     *
     * @param documentNumber the document number
     * @return Mono of Boolean - true if exists, false otherwise
     */
    Mono<Boolean> existsByDocumentNumber(String documentNumber);

    /**
     * Checks if a customer exists with the given email.
     *
     * @param email the email
     * @return Mono of Boolean - true if exists, false otherwise
     */
    Mono<Boolean> existsByEmail(String email);
}
