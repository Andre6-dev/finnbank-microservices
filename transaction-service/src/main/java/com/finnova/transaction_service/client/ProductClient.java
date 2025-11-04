package com.finnova.transaction_service.client;

import com.finnova.transaction_service.exception.ProductNotFoundException;
import com.finnova.transaction_service.model.dto.ActiveProductDto;
import com.finnova.transaction_service.model.dto.PassiveProductDto;
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

    private final WebClient webClient;

    @Value("${services.products-service.url:http://products-service}")
    private String productsServiceUrl;

    /**
     * Get a product by ID (tries active first, then passive)
     */
    public Mono<ProductDto> getProduct(String productId) {
        log.debug("Getting product: {}", productId);

        return Mono.firstWithValue(
                getPassiveProduct(productId),
                getActiveProduct(productId)
        ).switchIfEmpty(Mono.error(new ProductNotFoundException(
                "Product not found with ID: " + productId
        )));
    }

    /**
     * Get an active product (credit/credit card)
     */
    public Mono<ProductDto> getActiveProduct(String productId) {
        log.debug("Getting active product: {}", productId);

        return webClient
                .get()
                .uri(productsServiceUrl + "/active-products/{id}", productId)
                .retrieve()
                .bodyToMono(ActiveProductDto.class)
                .map(this::convertActiveToProductDto)
                .doOnSuccess(product -> log.debug("Retrieved active product {}: {}", productId, product))
                .doOnError(error -> log.error("Error retrieving active product {}: {}", productId, error.getMessage()));
    }

    /**
     * Get a passive product (savings/checking/fixed-term)
     */
    public Mono<ProductDto> getPassiveProduct(String productId) {
        log.debug("Getting passive product: {}", productId);

        return webClient
                .get()
                .uri(productsServiceUrl + "/passive-products/{id}", productId)
                .retrieve()
                .bodyToMono(PassiveProductDto.class)
                .map(this::convertPassiveToProductDto)
                .doOnSuccess(product -> log.debug("Retrieved passive product {}: {}", productId, product))
                .doOnError(error -> log.error("Error retrieving passive product {}: {}", productId, error.getMessage()));
    }

    /**
     * Update balance for passive products (deposit/withdrawal)
     */
    public Mono<ProductDto> updateBalance(String productId, BigDecimal newBalance) {
        log.debug("Updating balance for passive product {}: new balance {}", productId, newBalance);

        return webClient
                .post()
                .uri(productsServiceUrl + "/passive-products/{id}/deposit", productId)
                .bodyValue(Map.of("amount", newBalance))
                .retrieve()
                .bodyToMono(PassiveProductDto.class)
                .map(this::convertPassiveToProductDto)
                .doOnSuccess(product -> log.debug("Updated balance for product {}: {}", productId, product))
                .doOnError(error -> log.error("Error updating balance for product {}: {}", productId, error.getMessage()));
    }

    /**
     * Deposit to passive product
     */
    public Mono<ProductDto> deposit(String productId, BigDecimal amount) {
        log.debug("Depositing {} to product {}", amount, productId);

        return webClient
                .post()
                .uri(productsServiceUrl + "/passive-products/{id}/deposit", productId)
                .bodyValue(Map.of("amount", amount))
                .retrieve()
                .bodyToMono(PassiveProductDto.class)
                .map(this::convertPassiveToProductDto)
                .doOnSuccess(product -> log.debug("Deposit successful for product {}", productId))
                .doOnError(error -> log.error("Error depositing to product {}: {}", productId, error.getMessage()));
    }

    /**
     * Withdraw from passive product
     */
    public Mono<ProductDto> withdraw(String productId, BigDecimal amount) {
        log.debug("Withdrawing {} from product {}", amount, productId);

        return webClient
                .post()
                .uri(productsServiceUrl + "/passive-products/{id}/withdraw", productId)
                .bodyValue(Map.of("amount", amount))
                .retrieve()
                .bodyToMono(PassiveProductDto.class)
                .map(this::convertPassiveToProductDto)
                .doOnSuccess(product -> log.debug("Withdrawal successful for product {}", productId))
                .doOnError(error -> log.error("Error withdrawing from product {}: {}", productId, error.getMessage()));
    }

    /**
     * Make charge to active product (credit/credit card)
     */
    public Mono<ProductDto> makeCharge(String productId, BigDecimal amount) {
        log.debug("Making charge of {} to product {}", amount, productId);

        return webClient
                .post()
                .uri(productsServiceUrl + "/active-products/{id}/charge", productId)
                .bodyValue(Map.of("amount", amount))
                .retrieve()
                .bodyToMono(ActiveProductDto.class)
                .map(this::convertActiveToProductDto)
                .doOnSuccess(product -> log.debug("Charge successful for product {}", productId))
                .doOnError(error -> log.error("Error making charge to product {}: {}", productId, error.getMessage()));
    }

    /**
     * Make payment to active product (credit/credit card)
     */
    public Mono<ProductDto> makePayment(String productId, BigDecimal amount) {
        log.debug("Making payment of {} to product {}", amount, productId);

        return webClient
                .post()
                .uri(productsServiceUrl + "/active-products/{id}/payment", productId)
                .bodyValue(Map.of("amount", amount))
                .retrieve()
                .bodyToMono(ActiveProductDto.class)
                .map(this::convertActiveToProductDto)
                .doOnSuccess(product -> log.debug("Payment successful for product {}", productId))
                .doOnError(error -> log.error("Error making payment to product {}: {}", productId, error.getMessage()));
    }

    /**
     * Convert ActiveProductDto to ProductDto for compatibility
     */
    private ProductDto convertActiveToProductDto(ActiveProductDto active) {
        return ProductDto.builder()
                .id(active.getId())
                .customerId(active.getCustomerId())
                .productType(active.getProductType())
                .accountNumber(active.getCreditNumber())
                .balance(active.getUsedCredit())
                .creditLimit(active.getCreditLimit())
                .availableBalance(active.getAvailableCredit())
                .currency(active.getCurrency())
                .status(active.getStatus())
                .build();
    }

    /**
     * Convert PassiveProductDto to ProductDto for compatibility
     */
    private ProductDto convertPassiveToProductDto(PassiveProductDto passive) {
        return ProductDto.builder()
                .id(passive.getId())
                .customerId(passive.getCustomerId())
                .productType(passive.getProductType())
                .accountNumber(passive.getAccountNumber())
                .balance(passive.getBalance())
                .availableBalance(passive.getBalance())
                .currency(passive.getCurrency())
                .status(passive.getStatus())
                .monthlyTransactionCount(passive.getCurrentMonthTransactions())
                .maxFreeTransactions(passive.getMaxTransactionsWithoutFee())
                .build();
    }
}
