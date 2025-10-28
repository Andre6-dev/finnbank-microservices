package com.finnova.transaction_service.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventListener {

    /**
     * Listen to account created events
     * This could be used to initialize transaction tracking or other purposes
     */
    @KafkaListener(topics = "account-created", groupId = "transaction-service-group")
    public void handleAccountCreated(String message) {
        log.info("Received account created event: {}", message);
        // Implement logic if needed
    }

    /**
     * Listen to account updated events
     */
    @KafkaListener(topics = "account-updated", groupId = "transaction-service-group")
    public void handleAccountUpdated(String message) {
        log.info("Received account updated event: {}", message);
        // Implement logic if needed
    }

    /**
     * Listen to account blocked/closed events
     * Could be used to prevent new transactions
     */
    @KafkaListener(topics = "account-blocked", groupId = "transaction-service-group")
    public void handleAccountBlocked(String message) {
        log.info("Received account blocked event: {}", message);
        // Implement logic if needed - e.g., cache blocked accounts
    }
}
