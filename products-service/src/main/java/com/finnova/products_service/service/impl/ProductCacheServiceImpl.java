package com.finnova.products_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finnova.products_service.model.entity.ActiveProduct;
import com.finnova.products_service.model.entity.PassiveProduct;
import com.finnova.products_service.service.ProductCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCacheServiceImpl implements ProductCacheService {
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PASSIVE_PRODUCT_PREFIX = "passive_product:";
    private static final String ACTIVE_PRODUCT_PREFIX = "active_product:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    @Override
    public Mono<PassiveProduct> cachePassiveProduct(PassiveProduct product) {
        String key = PASSIVE_PRODUCT_PREFIX + product.getId();

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(product))
                .flatMap(json -> redisTemplate.opsForValue()
                        .set(key, json, CACHE_TTL)
                        .thenReturn(product))
                .doOnSuccess(p -> log.debug("Cached passive product: {}", product.getId()))
                .doOnError(e -> log.error("Error caching passive product: {}", e.getMessage()));
    }

    @Override
    public Mono<PassiveProduct> getPassiveProductFromCache(String productId) {
        String key = PASSIVE_PRODUCT_PREFIX + productId;

        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(json -> {
                    try {
                        PassiveProduct product = objectMapper.readValue(json, PassiveProduct.class);
                        log.debug("Retrieved passive product from cache: {}", productId);
                        return Mono.just(product);
                    } catch (JsonProcessingException e) {
                        log.error("Error deserializing passive product from cache: {}", e.getMessage());
                        return Mono.empty();
                    }
                });
    }

    @Override
    public Mono<Void> evictPassiveProductFromCache(String productId) {
        String key = PASSIVE_PRODUCT_PREFIX + productId;

        return redisTemplate.opsForValue()
                .delete(key)
                .then()
                .doOnSuccess(v -> log.debug("Evicted passive product from cache: {}", productId))
                .doOnError(e -> log.error("Error evicting passive product from cache: {}", e.getMessage()));
    }

    @Override
    public Mono<ActiveProduct> cacheActiveProduct(ActiveProduct product) {
        String key = ACTIVE_PRODUCT_PREFIX + product.getId();

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(product))
                .flatMap(json -> redisTemplate.opsForValue()
                        .set(key, json, CACHE_TTL)
                        .thenReturn(product))
                .doOnSuccess(p -> log.debug("Cached active product: {}", product.getId()))
                .doOnError(e -> log.error("Error caching active product: {}", e.getMessage()));
    }

    @Override
    public Mono<ActiveProduct> getActiveProductFromCache(String productId) {
        String key = ACTIVE_PRODUCT_PREFIX + productId;

        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(json -> {
                    try {
                        ActiveProduct product = objectMapper.readValue(json, ActiveProduct.class);
                        log.debug("Retrieved active product from cache: {}", productId);
                        return Mono.just(product);
                    } catch (JsonProcessingException e) {
                        log.error("Error deserializing active product from cache: {}", e.getMessage());
                        return Mono.empty();
                    }
                });
    }

    @Override
    public Mono<Void> evictActiveProductFromCache(String productId) {
        String key = ACTIVE_PRODUCT_PREFIX + productId;

        return redisTemplate.opsForValue()
                .delete(key)
                .then()
                .doOnSuccess(v -> log.debug("Evicted active product from cache: {}", productId))
                .doOnError(e -> log.error("Error evicting active product from cache: {}", e.getMessage()));
    }
}
