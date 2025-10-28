package com.finnova.products_service.event.publisher;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finnova.products_service.event.model.ActiveProductCreatedEvent;
import com.finnova.products_service.event.model.ActiveProductUpdatedEvent;
import com.finnova.products_service.event.model.BalanceChangedEvent;
import com.finnova.products_service.event.model.PassiveProductCreatedEvent;
import com.finnova.products_service.event.model.PassiveProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topic.passive-product-events}")
    private String passiveProductEventsTopic;

    @Value("${spring.kafka.topic.active-product-events}")
    private String activeProductEventsTopic;

    @Value("${spring.kafka.topic.balance-events}")
    private String balanceEventsTopic;

    /**
     * Publishes a passive product created event.
     *
     * @param event the passive product created event
     * @return Mono of Void
     */
    public Mono<Void> publishPassiveProductCreatedEvent(PassiveProductCreatedEvent event) {
        return Mono.fromRunnable(() -> {
            try {
                String message = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(passiveProductEventsTopic, "passive-product.created", message);
                log.info("Published PassiveProductCreatedEvent for product: {}", event.getProductId());
            } catch (JsonProcessingException e) {
                log.error("Error serializing PassiveProductCreatedEvent: {}", e.getMessage());
            }
        });
    }

    /**
     * Publishes a passive product updated event.
     *
     * @param event the passive product updated event
     * @return Mono of Void
     */
    public Mono<Void> publishPassiveProductUpdatedEvent(PassiveProductUpdatedEvent event) {
        return Mono.fromRunnable(() -> {
            try {
                String message = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(passiveProductEventsTopic, "passive-product.updated", message);
                log.info("Published PassiveProductUpdatedEvent for product: {}", event.getProductId());
            } catch (JsonProcessingException e) {
                log.error("Error serializing PassiveProductUpdatedEvent: {}", e.getMessage());
            }
        });
    }

    /**
     * Publishes an active product created event.
     *
     * @param event the active product created event
     * @return Mono of Void
     */
    public Mono<Void> publishActiveProductCreatedEvent(ActiveProductCreatedEvent event) {
        return Mono.fromRunnable(() -> {
            try {
                String message = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(activeProductEventsTopic, "active-product.created", message);
                log.info("Published ActiveProductCreatedEvent for product: {}", event.getProductId());
            } catch (JsonProcessingException e) {
                log.error("Error serializing ActiveProductCreatedEvent: {}", e.getMessage());
            }
        });
    }

    /**
     * Publishes an active product updated event.
     *
     * @param event the active product updated event
     * @return Mono of Void
     */
    public Mono<Void> publishActiveProductUpdatedEvent(ActiveProductUpdatedEvent event) {
        return Mono.fromRunnable(() -> {
            try {
                String message = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(activeProductEventsTopic, "active-product.updated", message);
                log.info("Published ActiveProductUpdatedEvent for product: {}", event.getProductId());
            } catch (JsonProcessingException e) {
                log.error("Error serializing ActiveProductUpdatedEvent: {}", e.getMessage());
            }
        });
    }

    /**
     * Publishes a balance changed event.
     *
     * @param event the balance changed event
     * @return Mono of Void
     */
    public Mono<Void> publishBalanceChangedEvent(BalanceChangedEvent event) {
        return Mono.fromRunnable(() -> {
            try {
                String message = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(balanceEventsTopic, "balance.changed", message);
                log.info("Published BalanceChangedEvent for product: {} - Operation: {}",
                        event.getProductId(), event.getOperationType());
            } catch (JsonProcessingException e) {
                log.error("Error serializing BalanceChangedEvent: {}", e.getMessage());
            }
        });
    }
}
