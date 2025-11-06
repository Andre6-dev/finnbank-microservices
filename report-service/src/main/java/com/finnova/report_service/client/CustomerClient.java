package com.finnova.report_service.client;

import com.finnova.report_service.model.dto.CustomerDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerClient {

    private final WebClient webClient;

    @CircuitBreaker(name = "customerService", fallbackMethod = "fallbackGetCustomer")
    @TimeLimiter(name = "customerService")
    public Mono<CustomerDto> getCustomer(String customerId) {
        log.info("Fetching customer: {}", customerId);
        return webClient.get()
                .uri("http://customer-service/api/customers/{id}", customerId)
                .retrieve()
                .bodyToMono(CustomerDto.class)
                .doOnError(error -> log.error("Error fetching customer: {}", customerId, error));
    }

    public Mono<CustomerDto> fallbackGetCustomer(String customerId, Exception ex) {
        log.error("Fallback: customer service unavailable for customer: {}", customerId, ex);
        return Mono.just(CustomerDto.builder()
                .id(customerId)
                .firstName("N/A")
                .lastName("(Service Unavailable)")
                .build());
    }
}
