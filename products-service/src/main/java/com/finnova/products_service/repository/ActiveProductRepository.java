package com.finnova.products_service.repository;

import com.finnova.products_service.model.entity.ActiveProduct;
import com.finnova.products_service.model.enums.ActiveProductType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ActiveProductRepository extends ReactiveMongoRepository<ActiveProduct, String> {

    /**
     * Finds an active product by credit number.
     *
     * @param creditNumber the credit number
     * @return Mono of ActiveProduct
     */
    Mono<ActiveProduct> findByCreditNumber(String creditNumber);

    /**
     * Finds all active products by customer ID.
     *
     * @param customerId the customer ID
     * @return Flux of ActiveProducts
     */
    Flux<ActiveProduct> findByCustomerId(String customerId);

    /**
     * Finds active products by customer ID and product type.
     *
     * @param customerId the customer ID
     * @param productType the product type
     * @return Flux of ActiveProducts
     */
    Flux<ActiveProduct> findByCustomerIdAndProductType(String customerId, ActiveProductType productType);

    /**
     * Counts active products by customer ID and product type.
     *
     * @param customerId the customer ID
     * @param productType the product type
     * @return Mono of Long
     */
    Mono<Long> countByCustomerIdAndProductType(String customerId, ActiveProductType productType);

    /**
     * Finds active products with overdue debt by customer ID.
     *
     * @param customerId the customer ID
     * @param hasOverdueDebt overdue debt flag
     * @return Flux of ActiveProducts
     */
    Flux<ActiveProduct> findByCustomerIdAndHasOverdueDebt(String customerId, Boolean hasOverdueDebt);

    /**
     * Checks if credit number exists.
     *
     * @param creditNumber the credit number
     * @return Mono of Boolean
     */
    Mono<Boolean> existsByCreditNumber(String creditNumber);
}
