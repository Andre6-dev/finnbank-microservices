package com.finnova.digital_wallet_service.client;

import com.finnova.digital_wallet_service.client.model.DebitCardDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
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
public class CardClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.card-service.url:http://card-service}")
    private String cardServiceUrl;

    @CircuitBreaker(name = "cardService", fallbackMethod = "fallbackGetDebitCard")
    @TimeLimiter(name = "cardService")
    public Mono<DebitCardDto> getDebitCard(String cardId) {
        log.debug("Getting debit card: {}", cardId);

        return webClientBuilder.build()
                .get()
                .uri(cardServiceUrl + "/debit-cards/{id}", cardId)
                .retrieve()
                .bodyToMono(DebitCardDto.class)
                .doOnSuccess(card -> log.debug("Retrieved debit card {}: {}", cardId, card))
                .doOnError(error -> log.error("Error retrieving debit card {}: {}", cardId, error.getMessage()));
    }

    @CircuitBreaker(name = "cardService", fallbackMethod = "fallbackWithdraw")
    @TimeLimiter(name = "cardService")
    public Mono<Void> withdrawFromDebitCard(String cardId, BigDecimal amount) {
        log.info("Withdrawing {} from debit card: {}", amount, cardId);

        Map<String, Object> request = Map.of(
                "amount", amount,
                "description", "Yanki load balance"
        );

        return webClientBuilder.build()
                .post()
                .uri(cardServiceUrl + "/debit-cards/{id}/withdrawal", cardId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Successfully withdrew {} from debit card: {}", amount
                        , cardId))
                .doOnError(error -> log.error("Error withdrawing {} from debit card {}: {}", amount, cardId, error.getMessage()));
    }

    @CircuitBreaker(name = "cardService", fallbackMethod = "fallbackDeposit")
    @TimeLimiter(name = "cardService")
    public Mono<Void> depositToDebitCard(String cardId, BigDecimal amount) {
        log.info("Depositing {} to debit card: {}", amount, cardId);

        Map<String, Object> request = Map.of(
                "amount", amount,
                "description", "Yanki load balance"
        );

        return webClientBuilder.build()
                .post()
                .uri(cardServiceUrl + "/debit-cards/{id}/deposit", cardId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Successfully deposited {} to debit card: {}", amount, cardId))
                .doOnError(error -> log.error("Error depositing {} to debit card {}: {}", amount, cardId, error.getMessage()));
    }

    // Fallback methods
    public Mono<DebitCardDto> fallbackGetDebitCard(String cardId, Exception ex) {
        log.error("Fallback: card service unavailable", ex);
        return Mono.error(new RuntimeException("Card service is unavailable"));
    }

    public Mono<Void> fallbackWithdraw(String cardId, BigDecimal amount, Exception ex) {
        log.error("Fallback: withdrawal failed", ex);
        return Mono.error(new RuntimeException("Card service is unavailable"));
    }

    public Mono<Void> fallbackDeposit(String cardId, BigDecimal amount, Exception ex) {
        log.error("Fallback: deposit failed", ex);
        return Mono.error(new RuntimeException("Card service is unavailable"));
    }
}
