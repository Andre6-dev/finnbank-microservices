package com.finnova.customer_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finnova.customer_service.model.entity.Customer;
import com.finnova.customer_service.service.CustomerCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class CustomerCacheServiceImpl implements CustomerCacheService {
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "customer:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    public CustomerCacheServiceImpl(
            @Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Builds the cache key for a customer.
     *
     * @param customerId the customer ID
     * @return the cache key
     */
    private String buildCacheKey(String customerId) {
        return CACHE_PREFIX + customerId;
    }

    @Override
    public Mono<Customer> cacheCustomer(Customer customer) {
        String key = buildCacheKey(customer.getId());

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(customer))
                .flatMap(json -> redisTemplate.opsForValue()
                        .set(key, json, CACHE_TTL)
                        .thenReturn(customer))
                .doOnSuccess(c -> log.debug("Cached customer: {}", customer.getId()))
                .doOnError(e -> log.error("Error caching customer: {}", e.getMessage()));
    }

    @Override
    public Mono<Customer> getFromCache(String customerId) {
        String key = buildCacheKey(customerId);

        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(json -> {
                    try {
                        Customer customer = objectMapper.readValue(json, Customer.class);
                        log.debug("Retrieved customer from cache: {}", customerId);
                        return Mono.just(customer);
                    } catch (JsonProcessingException e) {
                        log.error("Error deserializing customer from cache: {}", e.getMessage());
                        return Mono.empty();
                    }
                });
    }

    @Override
    public Mono<Void> evictFromCache(String customerId) {
        String key = buildCacheKey(customerId);

        return redisTemplate.opsForValue()
                .delete(key)
                .then()
                .doOnSuccess(v -> log.debug("Evicted customer from cache: {}", customerId))
                .doOnError(e -> log.error("Error evicting customer from cache: {}", e.getMessage()));
    }
}
