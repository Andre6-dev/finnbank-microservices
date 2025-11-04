package com.finnova.transaction_service.client;

import com.finnova.transaction_service.model.dto.CustomerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CustomerClient {

    private final WebClient customerServiceWebClient;

    @Value("${services.customer-service.url:http://customer-service}")
    private String customerServiceUrl;

    public CustomerClient(@Qualifier("customerServiceWebClient") WebClient customerServiceWebClient) {
        this.customerServiceWebClient = customerServiceWebClient;
    }

    public Mono<CustomerDto> getCustomer(String customerId) {
        log.debug("Getting customer: {}", customerId);

        return customerServiceWebClient
                .get()
                .uri(customerServiceUrl + "/customers/{id}", customerId)
                .retrieve()
                .bodyToMono(CustomerDto.class)
                .doOnSuccess(customer -> log.debug("Retrieved customer {}: {}", customerId, customer))
                .doOnError(error -> log.error("Error retrieving customer {}: {}", customerId, error.getMessage()));
    }

    public Mono<Boolean> hasOverdueDebt(String customerId) {
        log.debug("Checking overdue debt for customer: {}", customerId);

        return customerServiceWebClient
                .get()
                .uri(customerServiceUrl + "/customers/{id}/overdue-debt", customerId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnSuccess(hasDebt -> log.debug("Customer {} has overdue debt: {}", customerId, hasDebt))
                .doOnError(error -> log.error("Error checking overdue debt for customer {}: {}", customerId, error.getMessage()));
    }

    // Fallback methods
    public Mono<CustomerDto> fallbackGetCustomer(String customerId, Exception ex) {
        log.error("Fallback: customer service unavailable for customer: {}", customerId, ex);
        return Mono.error(new RuntimeException("Customer service is unavailable. Please try again later."));
    }

    public Mono<Boolean> fallbackHasOverdueDebt(String customerId, Exception ex) {
        log.error("Fallback: cannot check overdue debt for customer: {}", customerId, ex);
        // Por seguridad, asumir que NO tiene deuda vencida si el servicio no responde
        return Mono.just(false);
    }
}
