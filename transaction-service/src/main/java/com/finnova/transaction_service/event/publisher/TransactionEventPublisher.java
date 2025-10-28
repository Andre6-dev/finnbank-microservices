package com.finnova.transaction_service.event.publisher;

import com.finnova.transaction_service.config.KafkaTopicConfig;
import com.finnova.transaction_service.event.model.TransactionEvent;
import com.finnova.transaction_service.event.model.TransferEvent;
import com.finnova.transaction_service.model.entity.Transaction;
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
public class TransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish transaction created event
     */
    public Mono<Void> publishTransactionCreated(Transaction transaction) {
        return Mono.fromRunnable(() -> {
            TransactionEvent event = buildTransactionEvent(transaction, "TRANSACTION_CREATED");

            kafkaTemplate.send(KafkaTopicConfig.TRANSACTION_CREATED_TOPIC,
                            transaction.getTransactionNumber(),
                            event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Transaction created event published: {}", transaction.getTransactionNumber());
                        } else {
                            log.error("Failed to publish transaction created event: {}", transaction.getTransactionNumber(), ex);
                        }
                    });
        });
    }

    /**
     * Publish transaction completed event
     */
    public Mono<Void> publishTransactionCompleted(Transaction transaction) {
        return Mono.fromRunnable(() -> {
            TransactionEvent event = buildTransactionEvent(transaction, "TRANSACTION_COMPLETED");

            kafkaTemplate.send(KafkaTopicConfig.TRANSACTION_COMPLETED_TOPIC,
                            transaction.getTransactionNumber(),
                            event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Transaction completed event published: {}", transaction.getTransactionNumber());
                        } else {
                            log.error("Failed to publish transaction completed event: {}", transaction.getTransactionNumber(), ex);
                        }
                    });
        });
    }

    /**
     * Publish transaction failed event
     */
    public Mono<Void> publishTransactionFailed(Transaction transaction, String reason) {
        return Mono.fromRunnable(() -> {
            TransactionEvent event = buildTransactionEvent(transaction, "TRANSACTION_FAILED");
            event.setDescription(event.getDescription() + " - Failed reason: " + reason);

            kafkaTemplate.send(KafkaTopicConfig.TRANSACTION_FAILED_TOPIC,
                            transaction.getTransactionNumber(),
                            event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Transaction failed event published: {}", transaction.getTransactionNumber());
                        } else {
                            log.error("Failed to publish transaction failed event: {}", transaction.getTransactionNumber(), ex);
                        }
                    });
        });
    }

    /**
     * Publish transfer completed event
     */
    public Mono<Void> publishTransferCompleted(Transaction sourceTransaction, Transaction destTransaction) {
        return Mono.fromRunnable(() -> {
            TransferEvent event = TransferEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("TRANSFER_COMPLETED")
                    .timestamp(LocalDateTime.now())
                    .source("transaction-service")
                    .transferId(sourceTransaction.getId())
                    .transactionNumber(sourceTransaction.getTransactionNumber())
                    .sourceCustomerId(sourceTransaction.getCustomerId())
                    .sourceProductId(sourceTransaction.getProductId())
                    .destinationCustomerId(sourceTransaction.getDestinationCustomerId())
                    .destinationProductId(sourceTransaction.getDestinationProductId())
                    .amount(sourceTransaction.getAmount())
                    .transferType(determineTransferType(sourceTransaction, destTransaction))
                    .description(sourceTransaction.getDescription())
                    .status("COMPLETED")
                    .build();

            kafkaTemplate.send(KafkaTopicConfig.TRANSFER_COMPLETED_TOPIC,
                            sourceTransaction.getTransactionNumber(),
                            event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Transfer completed event published: {}", sourceTransaction.getTransactionNumber());
                        } else {
                            log.error("Failed to publish transfer completed event: {}", sourceTransaction.getTransactionNumber(), ex);
                        }
                    });
        });
    }

    /**
     * Publish transfer failed event
     */
    public Mono<Void> publishTransferFailed(String transactionNumber, String reason) {
        return Mono.fromRunnable(() -> {
            TransferEvent event = TransferEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("TRANSFER_FAILED")
                    .timestamp(LocalDateTime.now())
                    .source("transaction-service")
                    .transactionNumber(transactionNumber)
                    .description(reason)
                    .status("FAILED")
                    .build();

            kafkaTemplate.send(
                            KafkaTopicConfig.TRANSFER_FAILED_TOPIC,
                            transactionNumber,
                            event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Transfer failed event published: {}", transactionNumber);
                        } else {
                            log.error("Failed to publish transfer failed event: {}", transactionNumber, ex);
                        }
                    });
        });
    }

    // Helper methods

    private TransactionEvent buildTransactionEvent(Transaction transaction, String eventType) {
        return TransactionEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .source("transaction-service")
                .transactionId(transaction.getId())
                .transactionNumber(transaction.getTransactionNumber())
                .customerId(transaction.getCustomerId())
                .productId(transaction.getProductId())
                .productType(transaction.getProductType())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .commission(transaction.getCommission())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .transactionDate(transaction.getTransactionDate())
                .build();
    }

    private String determineTransferType(Transaction sourceTransaction, Transaction destTransaction) {
        if (sourceTransaction.getCustomerId().equals(destTransaction.getCustomerId())) {
            return "OWN_ACCOUNTS";
        }
        return "THIRD_PARTY";
    }
}
