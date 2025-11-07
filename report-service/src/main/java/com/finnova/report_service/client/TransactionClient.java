package com.finnova.report_service.client;

import com.finnova.report_service.model.dto.TransactionDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionClient {

    private final WebClient webClient;

    @CircuitBreaker(name = "transactionService", fallbackMethod = "fallbackGetTransactionsByCustomer")
    @TimeLimiter(name = "transactionService")
    public Flux<TransactionDto> getTransactionsByCustomer(String customerId) {
        log.info("Fetching transactions for customer: {}", customerId);
        return webClient.get()
                .uri("http://transaction-service/transactions/customer/{customerId}", customerId)
                .retrieve()
                .bodyToFlux(TransactionDto.class)
                .doOnError(error -> log.error("Error fetching transactions for customer: {}", customerId, error));
    }

    @CircuitBreaker(name = "transactionService", fallbackMethod = "fallbackGetTransactionsByProduct")
    @TimeLimiter(name = "transactionService")
    public Flux<TransactionDto> getTransactionsByProduct(String productId) {
        log.info("Fetching transactions for product: {}", productId);
        return webClient.get()
                .uri("http://transaction-service/transactions/product/{productId}", productId)
                .retrieve()
                .bodyToFlux(TransactionDto.class)
                .doOnError(error -> log.error("Error fetching transactions for product: {}", productId, error));
    }

    @CircuitBreaker(name = "transactionService", fallbackMethod = "fallbackGetTransactionsByProductAndDateRange")
    @TimeLimiter(name = "transactionService")
    public Flux<TransactionDto> getTransactionsByProductAndDateRange(
            String productId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        log.info("Fetching transactions for product: {} between {} and {}", productId, startDate, endDate);
        // Filtramos del lado del cliente ya que el endpoint no tiene query params para fechas
        return webClient.get()
                .uri("http://transaction-service/transactions/product/{productId}", productId)
                .retrieve()
                .bodyToFlux(TransactionDto.class)
                .filter(tx -> !tx.getTransactionDate().isBefore(startDate)
                        && !tx.getTransactionDate().isAfter(endDate))
                .doOnError(error -> log.error("Error fetching transactions for product in date range: {}", productId, error));
    }

    // Fallback methods
    public Flux<TransactionDto> fallbackGetTransactionsByCustomer(String customerId, Exception ex) {
        log.error("Fallback: transaction service unavailable for customer: {}", customerId, ex);
        return Flux.empty();
    }

    public Flux<TransactionDto> fallbackGetTransactionsByProduct(String productId, Exception ex) {
        log.error("Fallback: transaction service unavailable for product: {}", productId, ex);
        return Flux.empty();
    }

    public Flux<TransactionDto> fallbackGetTransactionsByProductAndDateRange(
            String productId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Exception ex
    ) {
        log.error("Fallback: transaction service unavailable for product in date range: {}", productId, ex);
        return Flux.empty();
    }
}
