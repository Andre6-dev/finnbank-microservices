package com.finnova.card_service.client;

import com.finnova.card_service.model.dto.TransactionDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Component
@Slf4j
public class TransactionClient {

    private final WebClient transactionServiceWebClient;

    @Value("${services.transaction-service.url:http://transaction-service}")
    private String transactionServiceUrl;

    public TransactionClient(@Qualifier("transactionServiceWebClient") WebClient transactionServiceWebClient) {
        this.transactionServiceWebClient = transactionServiceWebClient;
    }

    @CircuitBreaker(name = "transactionService", fallbackMethod = "fallbackWithdrawal")
    @TimeLimiter(name = "transactionService")
    public Mono<TransactionDto> withdrawal(String accountId, BigDecimal amount, String description) {
        log.info("Initiating withdrawal of {} from account: {}", amount, accountId);

        return transactionServiceWebClient
                .post()
                .uri(transactionServiceUrl + "/transactions/withdrawal")
                .bodyValue(Map.of(
                        "productId", accountId,
                        "amount", amount,
                        "description", description
                ))
                .retrieve()
                .bodyToMono(TransactionDto.class)
                .doOnSuccess(transaction -> log.info("Withdrawal successful: {}", transaction))
                .doOnError(error -> log.error("Error during withdrawal from account {}: {}", accountId, error.getMessage()));
    }

    /**
     * Get last 10 movements for an account
     */
    @CircuitBreaker(name = "transactionService", fallbackMethod = "fallbackGetMovements")
    @TimeLimiter(name = "transactionService")
    public Flux<TransactionDto> getLast10Movements(String accountId) {
        log.info("Fetching last 10 movements for account: {}", accountId);

        return transactionServiceWebClient
                .get()
                .uri(transactionServiceUrl + "/transactions/product/{productId}/last10", accountId)
                .retrieve()
                .bodyToFlux(TransactionDto.class)
                .doOnComplete(() -> log.info("Fetched last 10 movements for account: {}", accountId))
                .doOnError(error -> log.error("Error fetching movements for account {}: {}", accountId, error.getMessage()));
    }

    // Fallback methods
    public Mono<TransactionDto> fallbackWithdrawal(String accountId, BigDecimal amount, String description, Exception ex) {
        log.error("Fallback: withdrawal failed for account: {}", accountId, ex);
        return Mono.error(new RuntimeException("Transaction service is unavailable. Please try again later."));
    }

    public Flux<TransactionDto> fallbackGetMovements(String accountId, Exception ex) {
        log.error("Fallback: cannot get movements for account: {}", accountId, ex);
        return Flux.empty();
    }
}
