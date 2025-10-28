package com.finnova.products_service.repository;

import com.finnova.products_service.model.entity.PassiveProduct;
import com.finnova.products_service.model.enums.PassiveProductType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PassiveProductRepository extends ReactiveMongoRepository<PassiveProduct, String> {

    /**
     * Finds a passive product by account number.
     *
     * @param accountNumber the account number
     * @return Mono of PassiveProduct
     */
    Mono<PassiveProduct> findByAccountNumber(String accountNumber);

    /**
     * Finds all passive products by customer ID.
     *
     * @param customerId the customer ID
     * @return Flux of PassiveProducts
     */
    Flux<PassiveProduct> findByCustomerId(String customerId);

    /**
     * Finds passive products by customer ID and product type.
     *
     * @param customerId the customer ID
     * @param productType the product type
     * @return Flux of PassiveProducts
     */
    Flux<PassiveProduct> findByCustomerIdAndProductType(String customerId, PassiveProductType productType);

    /**
     * Counts passive products by customer ID and product type.
     *
     * @param customerId the customer ID
     * @param productType the product type
     * @return Mono of Long
     */
    Mono<Long> countByCustomerIdAndProductType(String customerId, PassiveProductType productType);

    /**
     * Checks if account number exists.
     *
     * @param accountNumber the account number
     * @return Mono of Boolean
     */
    Mono<Boolean> existsByAccountNumber(String accountNumber);
}
