package com.finnova.customer_service.event.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finnova.customer_service.event.model.CustomerCreatedEvent;
import com.finnova.customer_service.event.model.CustomerDeletedEvent;
import com.finnova.customer_service.event.model.CustomerUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CustomerEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topic.customer-events}")
    private String customerEventsTopic;

    public CustomerEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes a customer created event.
     *
     * @param event the customer created event
     * @return Mono of Void
     */
    public Mono<Void> publishCustomerCreatedEvent(CustomerCreatedEvent event) {
        return Mono.fromRunnable(() -> {
            try {
                String message = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(customerEventsTopic, "customer.created", message);
                log.info("Published CustomerCreatedEvent for customer: {}", event.getCustomerId());
            } catch (JsonProcessingException e) {
                log.error("Error serializing CustomerCreatedEvent: {}", e.getMessage());
            }
        });
    }

    /**
     * Publishes a customer updated event.
     *
     * @param event the customer updated event
     * @return Mono of Void
     */
    public Mono<Void> publishCustomerUpdatedEvent(CustomerUpdatedEvent event) {
        return Mono.fromRunnable(() -> {
            try {
                String message = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(customerEventsTopic, "customer.updated", message);
                log.info("Published CustomerUpdatedEvent for customer: {}", event.getCustomerId());
            } catch (JsonProcessingException e) {
                log.error("Error serializing CustomerUpdatedEvent: {}", e.getMessage());
            }
        });
    }

    /**
     * Publishes a customer deleted event.
     *
     * @param event the customer deleted event
     * @return Mono of Void
     */
    public Mono<Void> publishCustomerDeletedEvent(CustomerDeletedEvent event) {
        return Mono.fromRunnable(() -> {
            try {
                String message = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(customerEventsTopic, "customer.deleted", message);
                log.info("Published CustomerDeletedEvent for customer: {}", event.getCustomerId());
            } catch (JsonProcessingException e) {
                log.error("Error serializing CustomerDeletedEvent: {}", e.getMessage());
            }
        });
    }
}
