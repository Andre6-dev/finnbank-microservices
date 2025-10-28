package com.finnova.customer_service.service;

import com.finnova.customer_service.model.entity.Customer;
import reactor.core.publisher.Mono;

public interface CustomerCacheService {

    /**
     * Caches a customer.
     *
     * @param customer the customer to cache
     * @return Mono of Customer
     */
    Mono<Customer> cacheCustomer(Customer customer);

    /**
     * Gets a customer from cache.
     *
     * @param customerId the customer ID
     * @return Mono of Customer if found in cache, empty Mono otherwise
     */
    Mono<Customer> getFromCache(String customerId);

    /**
     * Evicts a customer from cache.
     *
     * @param customerId the customer ID
     * @return Mono of Void
     */
    Mono<Void> evictFromCache(String customerId);
}
