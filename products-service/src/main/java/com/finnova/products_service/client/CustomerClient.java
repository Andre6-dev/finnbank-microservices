package com.finnova.products_service.client;

import com.finnova.products_service.client.dto.CustomerResponse;
import com.finnova.products_service.client.dto.CustomerValidationResponse;
import com.finnova.products_service.exception.CustomerValidationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerClient {

    private final WebClient webClient;

    @Value("${services.customer-service.url:http://customer-service}")
    private String customerServiceUrl;

    /**
     * Validates if a customer exists and is active.
     *
     * @param customerId the customer ID
     * @return Mono of Boolean - true if valid, false otherwise
     */
    @CircuitBreaker(name = "customerService", fallbackMethod = "validateCustomerFallback")
    @TimeLimiter(name = "customerService")
    public Mono<Boolean> validateCustomer(String customerId) {
        log.debug("Validating customer: {}", customerId);

        return webClient
                .get()
                .uri(customerServiceUrl + "/customers/{id}/validate", customerId)
                .retrieve()
                .bodyToMono(CustomerValidationResponse.class)
                .timeout(Duration.ofSeconds(5))
                .map(CustomerValidationResponse::getValid)
                .doOnSuccess(valid -> log.debug("Customer {} validation result: {}", customerId, valid))
                .doOnError(error -> log.error("Error validating customer {}: {}", customerId, error.getMessage()));
    }

    /**
     * Gets customer information.
     *
     * @param customerId the customer ID
     * @return Mono of CustomerResponse
     */
    @CircuitBreaker(name = "customerService", fallbackMethod = "getCustomerFallback")
    @TimeLimiter(name = "customerService")
    public Mono<CustomerResponse> getCustomer(String customerId) {
        log.debug("Getting customer: {}", customerId);

        return webClient
                .get()
                .uri(customerServiceUrl + "/customers/{id}", customerId)
                .retrieve()
                .bodyToMono(CustomerResponse.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(customer -> log.debug("Customer retrieved: {}", customerId))
                .doOnError(error -> log.error("Error getting customer {}: {}", customerId, error.getMessage()));
    }

    /**
     * Fallback method for validateCustomer.
     *
     * @param customerId the customer ID
     * @param throwable the exception
     * @return Mono of Boolean - false (customer not validated)
     */
    private Mono<Boolean> validateCustomerFallback(String customerId, Throwable throwable) {
        log.error("Customer validation fallback triggered for customer {}: {}",
                customerId, throwable.getMessage());
        return Mono.error(new CustomerValidationException(
                "Unable to validate customer: " + customerId + ". Service temporarily unavailable."));
    }

    /**
     * Fallback method for getCustomer.
     *
     * @param customerId the customer ID
     * @param throwable the exception
     * @return Mono error
     */
    private Mono<CustomerResponse> getCustomerFallback(String customerId, Throwable throwable) {
        log.error("Get customer fallback triggered for customer {}: {}",
                customerId, throwable.getMessage());
        return Mono.error(new CustomerValidationException(
                "Unable to get customer: " + customerId + ". Service temporarily unavailable."));
    }
}
