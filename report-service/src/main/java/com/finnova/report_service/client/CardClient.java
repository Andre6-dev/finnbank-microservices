package com.finnova.report_service.client;

import com.finnova.report_service.model.dto.DebitCardDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardClient {

    private final WebClient webClient;

    @CircuitBreaker(name = "cardService", fallbackMethod = "fallbackGetDebitCardsByCustomer")
    @TimeLimiter(name = "cardService")
    public Flux<DebitCardDto> getDebitCardsByCustomer(String customerId) {
        log.info("Fetching debit cards for customer: {}", customerId);
        return webClient.get()
                .uri("http://card-service/debit-cards/customer/{customerId}", customerId)
                .retrieve()
                .bodyToFlux(DebitCardDto.class)
                .doOnError(error -> log.error("Error fetching debit cards for customer: {}", customerId, error));
    }

    public Flux<DebitCardDto> fallbackGetDebitCardsByCustomer(String customerId, Exception ex) {
        log.error("Fallback: card service unavailable for customer: {}", customerId, ex);
        return Flux.empty();
    }
}
