package com.finnova.transaction_service.client;

import com.finnova.transaction_service.model.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.products-service.url:http://products-service}")
    private String productsServiceUrl;

    public Mono<ProductDto> getProduct(String productId) {
        log.debug("Getting product: {}", productId);

        return webClientBuilder.build()
                .get()
                .uri(productsServiceUrl + "/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .doOnSuccess(product -> log.debug("Retrieved product {}: {}", productId, product))
                .doOnError(error -> log.error("Error retrieving product {}: {}", productId, error.getMessage()));
    }

    public Mono<ProductDto> updateBalance(String productId, BigDecimal newBalance) {
        log.debug("Updating balance for product {}: new balance {}", productId, newBalance);

        return webClientBuilder.build()
                .put()
                .uri(productsServiceUrl + "/products/{id}/balance", productId)
                .bodyValue(Map.of("balance", newBalance))
                .retrieve()
                .bodyToMono(ProductDto.class)
                .doOnSuccess(product -> log.debug("Updated balance for product {}: {}", productId, product))
                .doOnError(error -> log.error("Error updating balance for product {}: {}", productId, error.getMessage()));
    }
}
