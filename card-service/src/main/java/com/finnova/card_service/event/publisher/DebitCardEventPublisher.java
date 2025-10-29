package com.finnova.card_service.event.publisher;

import com.finnova.card_service.event.model.DebitCardEvent;
import com.finnova.card_service.model.entity.DebitCard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DebitCardEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String CARD_CREATED_TOPIC = "debit-card-created";
    private static final String ACCOUNT_ASSOCIATED_TOPIC = "debit-card-account-associated";

    /**
     * Publish card created event
     */
    public Mono<Void> publishCardCreated(DebitCard card) {
        return Mono.fromRunnable(() -> {
            DebitCardEvent event = DebitCardEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("DEBIT_CARD_CREATED")
                    .timestamp(LocalDateTime.now())
                    .source("card-service")
                    .cardId(card.getId())
                    .cardNumber(card.getCardNumber())
                    .customerId(card.getCustomerId())
                    .status(card.getStatus().name())
                    .build();

            kafkaTemplate.send(CARD_CREATED_TOPIC, card.getId(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Debit card created event published: {}", card.getId());
                        } else {
                            log.error("Failed to publish card created event: {}", card.getId(), ex);
                        }
                    });
        });
    }

    /**
     * Publish account associated event
     */
    public Mono<Void> publishAccountAssociated(DebitCard card, String accountId) {
        return Mono.fromRunnable(() -> {
            DebitCardEvent event = DebitCardEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("DEBIT_CARD_ACCOUNT_ASSOCIATED")
                    .timestamp(LocalDateTime.now())
                    .source("card-service")
                    .cardId(card.getId())
                    .cardNumber(card.getCardNumber())
                    .customerId(card.getCustomerId())
                    .accountId(accountId)
                    .status(card.getStatus().name())
                    .build();

            kafkaTemplate.send(ACCOUNT_ASSOCIATED_TOPIC, card.getId(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Account associated event published: card={}, account={}",
                                    card.getId(), accountId);
                        } else {
                            log.error("Failed to publish account associated event: card={}, account={}",
                                    card.getId(), accountId, ex);
                        }
                    });
        });
    }
}
