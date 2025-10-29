package com.finnova.card_service.client;

import com.finnova.card_service.model.dto.ProductDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.product-service.url:http://products-service}")
    private String productServiceUrl;

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetAccount")
    @TimeLimiter(name = "productService")
    public Mono<ProductDto> getAccount(String accountId) {
        log.info("Fetching account: {}", accountId);

        return webClientBuilder.build()
                .get()
                .uri(productServiceUrl + "/products/accounts/{id}", accountId)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .doOnSuccess(product -> log.info("Fetched account: {}", product))
                .doOnError(error -> log.error("Error fetching account {}: {}", accountId, error.getMessage()));
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetAccountBalance")
    @TimeLimiter(name = "productService")
    public Mono<BigDecimal> getAccountBalance(String accountId) {
        log.info("Fetching account balance for: {}", accountId);

        return webClientBuilder.build()
                .get()
                .uri(productServiceUrl + "/products/accounts/{id}/balance", accountId)
                .retrieve()
                .bodyToMono(BigDecimal.class)
                .doOnSuccess(balance -> log.info("Fetched account balance: {}", balance))
                .doOnError(error -> log.error("Error fetching account balance for {}: {}", accountId, error.getMessage()));
    }

    // Fallback methods
    public Mono<ProductDto> fallbackGetAccount(String accountId, Exception ex) {
        log.error("Fallback: product service unavailable for account: {}", accountId, ex);
        return Mono.error(new RuntimeException("Product service is unavailable. Please try again later."));
    }

    public Mono<BigDecimal> fallbackGetAccountBalance(String accountId, Exception ex) {
        log.error("Fallback: cannot get balance for account: {}", accountId, ex);
        return Mono.error(new RuntimeException("Product service is unavailable. Please try again later."));
    }
}
