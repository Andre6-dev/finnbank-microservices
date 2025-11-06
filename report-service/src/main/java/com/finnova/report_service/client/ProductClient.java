package com.finnova.report_service.client;

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

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetProduct")
    @TimeLimiter(name = "productService")
    public Mono<ProductDto> getProduct(String productId) {
        log.info("Fetching product: {}", productId);
        return webClient.get()
                .uri("http://products-service/api/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .doOnError(error -> log.error("Error fetching product: {}", productId, error));
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetProductsByCustomer")
    @TimeLimiter(name = "productService")
    public Flux<ProductDto> getProductsByCustomer(String customerId) {
        log.info("Fetching products for customer: {}", customerId);
        return webClient.get()
                .uri("http://products-service/api/products/customer/{customerId}", customerId)
                .retrieve()
                .bodyToFlux(ProductDto.class)
                .doOnError(error -> log.error("Error fetching products for customer: {}", customerId, error));
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetProductsByType")
    @TimeLimiter(name = "productService")
    public Flux<ProductDto> getProductsByType(String productType) {
        log.info("Fetching products by type: {}", productType);
        return webClient.get()
                .uri("http://products-service/api/products/type/{productType}", productType)
                .retrieve()
                .bodyToFlux(ProductDto.class)
                .doOnError(error -> log.error("Error fetching products by type: {}", productType, error));
    }

    // Fallback methods
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
