package com.finnova.card_service.client;

import com.finnova.card_service.model.dto.BalanceDto;
import com.finnova.card_service.model.dto.PassiveProductDto;
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

    private final WebClient webClient;

    @Value("${services.products-service.url:http://products-service}")
    private String productServiceUrl;

    /**
     * Gets a passive product (savings/checking account) by ID.
     * Debit cards are linked to passive products.
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetAccount")
    @TimeLimiter(name = "productService")
    public Mono<ProductDto> getAccount(String accountId) {
        log.info("Fetching passive product (account): {}", accountId);

        return webClient
                .get()
                .uri(productServiceUrl + "/passive-products/{id}", accountId)
                .retrieve()
                .bodyToMono(PassiveProductDto.class)
                .map(this::convertPassiveToProductDto)
                .doOnSuccess(product -> log.info("Fetched passive product: {}", product))
                .doOnError(error -> log.error("Error fetching account {}: {}", accountId, error.getMessage()));
    }

    /**
     * Gets the balance of a passive product.
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetAccountBalance")
    @TimeLimiter(name = "productService")
    public Mono<BigDecimal> getAccountBalance(String accountId) {
        log.info("Fetching account balance for: {}", accountId);

        return webClient
                .get()
                .uri(productServiceUrl + "/passive-products/{id}/balance", accountId)
                .retrieve()
                .bodyToMono(BalanceDto.class)
                .map(BalanceDto::getBalance)
                .doOnSuccess(balance -> log.info("Fetched account balance: {}", balance))
                .doOnError(error -> log.error("Error fetching account balance for {}: {}", accountId, error.getMessage()));
    }

    /**
     * Convert PassiveProductDto to ProductDto for compatibility.
     */
    private ProductDto convertPassiveToProductDto(PassiveProductDto passive) {
        return ProductDto.builder()
                .id(passive.getId())
                .customerId(passive.getCustomerId())
                .productType(passive.getProductType())
                .accountNumber(passive.getAccountNumber())
                .balance(passive.getBalance())
                .status(passive.getStatus())
                .build();
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
