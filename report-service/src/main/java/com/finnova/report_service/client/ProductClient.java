package com.finnova.report_service.client;

import com.finnova.report_service.model.dto.ActiveProductDto;
import com.finnova.report_service.model.dto.PassiveProductDto;
import com.finnova.report_service.model.dto.ProductDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClient {

    private final WebClient webClient;

    /**
     * Get a product by ID (tries passive first, then active)
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetProduct")
    @TimeLimiter(name = "productService")
    public Mono<ProductDto> getProduct(String productId) {
        log.info("Fetching product: {}", productId);

        return getPassiveProduct(productId)
                .onErrorResume(error -> {
                    log.debug("Product {} not found in passive products, trying active", productId);
                    return getActiveProduct(productId);
                });
    }

    /**
     * Get passive product by ID
     */
    private Mono<ProductDto> getPassiveProduct(String productId) {
        return webClient.get()
                .uri("http://products-service/passive-products/{id}", productId)
                .retrieve()
                .bodyToMono(PassiveProductDto.class)
                .map(this::convertPassiveToProductDto);
    }

    /**
     * Get active product by ID
     */
    private Mono<ProductDto> getActiveProduct(String productId) {
        return webClient.get()
                .uri("http://products-service/active-products/{id}", productId)
                .retrieve()
                .bodyToMono(ActiveProductDto.class)
                .map(this::convertActiveToProductDto);
    }

    /**
     * Get all products for a customer (both passive and active)
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetProductsByCustomer")
    @TimeLimiter(name = "productService")
    public Flux<ProductDto> getProductsByCustomer(String customerId) {
        log.info("Fetching products for customer: {}", customerId);

        Flux<ProductDto> passiveProducts = webClient.get()
                .uri("http://products-service/passive-products/customer/{customerId}", customerId)
                .retrieve()
                .bodyToFlux(PassiveProductDto.class)
                .map(this::convertPassiveToProductDto)
                .onErrorResume(error -> {
                    log.warn("Error fetching passive products for customer {}: {}", customerId, error.getMessage());
                    return Flux.empty();
                });

        Flux<ProductDto> activeProducts = webClient.get()
                .uri("http://products-service/active-products/customer/{customerId}", customerId)
                .retrieve()
                .bodyToFlux(ActiveProductDto.class)
                .map(this::convertActiveToProductDto)
                .onErrorResume(error -> {
                    log.warn("Error fetching active products for customer {}: {}", customerId, error.getMessage());
                    return Flux.empty();
                });

        return Flux.concat(passiveProducts, activeProducts);
    }

    /**
     * Get products by type
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetProductsByType")
    @TimeLimiter(name = "productService")
    public Flux<ProductDto> getProductsByType(String productType) {
        log.info("Fetching products by type: {}", productType);

        // Determine if it's passive or active product type
        if (isPassiveProductType(productType)) {
            return webClient.get()
                    .uri("http://products-service/passive-products")
                    .retrieve()
                    .bodyToFlux(PassiveProductDto.class)
                    .filter(p -> productType.equals(p.getProductType()))
                    .map(this::convertPassiveToProductDto);
        } else if (isActiveProductType(productType)) {
            return webClient.get()
                    .uri("http://products-service/active-products")
                    .retrieve()
                    .bodyToFlux(ActiveProductDto.class)
                    .filter(p -> productType.equals(p.getProductType()))
                    .map(this::convertActiveToProductDto);
        } else {
            log.warn("Unknown product type: {}", productType);
            return Flux.empty();
        }
    }

    // ========== CONVERSION METHODS ==========

    private ProductDto convertPassiveToProductDto(PassiveProductDto passive) {
        return ProductDto.builder()
                .id(passive.getId())
                .customerId(passive.getCustomerId())
                .productType(passive.getProductType())
                .accountNumber(passive.getAccountNumber())
                .balance(passive.getBalance())
                .availableBalance(passive.getBalance())
                .status(passive.getStatus())
                .monthlyTransactionCount(passive.getCurrentMonthTransactions())
                .maxFreeTransactions(passive.getMaxTransactionsWithoutFee())
                .createdAt(passive.getCreatedAt())
                .build();
    }

    private ProductDto convertActiveToProductDto(ActiveProductDto active) {
        return ProductDto.builder()
                .id(active.getId())
                .customerId(active.getCustomerId())
                .productType(active.getProductType())
                .accountNumber(active.getCreditNumber())
                .balance(active.getUsedCredit())
                .creditLimit(active.getCreditLimit())
                .availableBalance(active.getAvailableCredit())
                .status(active.getStatus())
                .createdAt(active.getCreatedAt())
                .build();
    }

    private boolean isPassiveProductType(String productType) {
        return "SAVINGS".equals(productType)
                || "CHECKING".equals(productType)
                || "FIXED_TERM".equals(productType);
    }

    private boolean isActiveProductType(String productType) {
        return "CREDIT".equals(productType)
                || "CREDIT_CARD".equals(productType);
    }

    // ========== FALLBACK METHODS ==========

    public Mono<ProductDto> fallbackGetProduct(String productId, Exception ex) {
        log.error("Fallback: product service unavailable for product: {}", productId, ex);
        return Mono.empty();
    }

    public Flux<ProductDto> fallbackGetProductsByCustomer(String customerId, Exception ex) {
        log.error("Fallback: product service unavailable for customer: {}", customerId, ex);
        return Flux.empty();
    }

    public Flux<ProductDto> fallbackGetProductsByType(String productType, Exception ex) {
        log.error("Fallback: product service unavailable for type: {}", productType, ex);
        return Flux.empty();
    }
}
