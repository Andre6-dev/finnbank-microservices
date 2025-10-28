package com.finnova.products_service.service;

import com.finnova.products_service.model.entity.ActiveProduct;
import com.finnova.products_service.model.entity.PassiveProduct;
import reactor.core.publisher.Mono;

public interface ProductCacheService {

    /**
     * Caches a passive product.
     *
     * @param product the passive product to cache
     * @return Mono of PassiveProduct
     */
    Mono<PassiveProduct> cachePassiveProduct(PassiveProduct product);

    /**
     * Gets a passive product from cache.
     *
     * @param productId the product ID
     * @return Mono of PassiveProduct if found in cache, empty Mono otherwise
     */
    Mono<PassiveProduct> getPassiveProductFromCache(String productId);

    /**
     * Evicts a passive product from cache.
     *
     * @param productId the product ID
     * @return Mono of Void
     */
    Mono<Void> evictPassiveProductFromCache(String productId);

    /**
     * Caches an active product.
     *
     * @param product the active product to cache
     * @return Mono of ActiveProduct
     */
    Mono<ActiveProduct> cacheActiveProduct(ActiveProduct product);

    /**
     * Gets an active product from cache.
     *
     * @param productId the product ID
     * @return Mono of ActiveProduct if found in cache, empty Mono otherwise
     */
    Mono<ActiveProduct> getActiveProductFromCache(String productId);

    /**
     * Evicts an active product from cache.
     *
     * @param productId the product ID
     * @return Mono of Void
     */
    Mono<Void> evictActiveProductFromCache(String productId);
}
