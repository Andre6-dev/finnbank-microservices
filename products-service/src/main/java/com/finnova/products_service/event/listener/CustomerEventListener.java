package com.finnova.products_service.event.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finnova.products_service.model.enums.ProductStatus;
import com.finnova.products_service.repository.ActiveProductRepository;
import com.finnova.products_service.repository.PassiveProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerEventListener {

    private final PassiveProductRepository passiveProductRepository;
    private final ActiveProductRepository activeProductRepository;
    private final ObjectMapper objectMapper;

    /**
     * Listens to customer events from customer-service.
     *
     * @param message the Kafka message
     */
    @KafkaListener(
            topics = "${spring.kafka.topic.customer-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleCustomerEvent(String message) {
        try {
            log.info("Received customer event: {}", message);

            JsonNode event = objectMapper.readTree(message);
            String customerId = event.get("customerId").asText();
            String eventType = determineEventType(event);

            switch (eventType) {
                case "CUSTOMER_CREATED":
                    handleCustomerCreated(customerId);
                    break;
                case "CUSTOMER_UPDATED":
                    handleCustomerUpdated(event);
                    break;
                case "CUSTOMER_DELETED":
                    handleCustomerDeleted(customerId);
                    break;
                default:
                    log.warn("Unknown customer event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing customer event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles customer created event.
     * Currently just logs the event, can be extended for specific business logic.
     *
     * @param customerId the customer ID
     */
    private void handleCustomerCreated(String customerId) {
        log.info("Customer created event received for customer: {}", customerId);
        // Could be extended to create default products for new customers
    }

    /**
     * Handles customer updated event.
     * Updates products if customer status changes.
     *
     * @param event the customer event
     */
    private void handleCustomerUpdated(JsonNode event) {
        try {
            String customerId = event.get("customerId").asText();
            boolean isActive = event.has("active") && event.get("active").asBoolean();

            log.info("Customer updated event received for customer: {} - Active: {}",
                    customerId, isActive);

            if (!isActive) {
                // Block all products if customer becomes inactive
                blockCustomerProducts(customerId)
                        .subscribe(
                                count -> log.info("Blocked {} products for inactive customer: {}",
                                        count, customerId),
                                error -> log.error("Error blocking products for customer {}: {}",
                                        customerId, error.getMessage())
                        );
            }
        } catch (Exception e) {
            log.error("Error handling customer updated event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles customer deleted event.
     * Blocks all products associated with the deleted customer.
     *
     * @param customerId the customer ID
     */
    private void handleCustomerDeleted(String customerId) {
        log.info("Customer deleted event received for customer: {}", customerId);

        blockCustomerProducts(customerId)
                .subscribe(
                        count -> log.info("Blocked {} products for deleted customer: {}",
                                count, customerId),
                        error -> log.error("Error blocking products for deleted customer {}: {}",
                                customerId, error.getMessage())
                );
    }

    /**
     * Blocks all products (passive and active) for a customer.
     *
     * @param customerId the customer ID
     * @return Mono of total count of blocked products
     */
    private Mono<Long> blockCustomerProducts(String customerId) {
        Mono<Long> passiveCount = passiveProductRepository.findByCustomerId(customerId)
                .flatMap(product -> {
                    product.setStatus(ProductStatus.BLOCKED);
                    product.setUpdatedAt(LocalDateTime.now());
                    return passiveProductRepository.save(product);
                })
                .count();

        Mono<Long> activeCount = activeProductRepository.findByCustomerId(customerId)
                .flatMap(product -> {
                    product.setStatus(ProductStatus.BLOCKED);
                    product.setUpdatedAt(LocalDateTime.now());
                    return activeProductRepository.save(product);
                })
                .count();

        return Mono.zip(passiveCount, activeCount)
                .map(tuple -> tuple.getT1() + tuple.getT2());
    }

    /**
     * Determines the event type from the event structure.
     *
     * @param event the event JSON node
     * @return the event type string
     */
    private String determineEventType(JsonNode event) {
        // Try to determine event type from the event structure
        if (event.has("timestamp") && !event.has("active")) {
            // Likely a creation event if it has timestamp but no active field changes
            return "CUSTOMER_CREATED";
        } else if (event.has("active")) {
            // Update event if it has active status
            return "CUSTOMER_UPDATED";
        } else if (event.has("customerId") && event.size() <= 3) {
            // Deletion event typically has minimal fields
            return "CUSTOMER_DELETED";
        }
        return "UNKNOWN";
    }
}
