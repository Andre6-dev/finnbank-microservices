package com.finnova.transaction_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String TRANSACTION_CREATED_TOPIC = "transaction-created";
    public static final String TRANSACTION_COMPLETED_TOPIC = "transaction-completed";
    public static final String TRANSACTION_FAILED_TOPIC = "transaction-failed";
    public static final String TRANSFER_COMPLETED_TOPIC = "transfer-completed";
    public static final String TRANSFER_FAILED_TOPIC = "transfer-failed";

    @Bean
    public NewTopic transactionCreatedTopic() {
        return TopicBuilder.name(TRANSACTION_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionCompletedTopic() {
        return TopicBuilder.name(TRANSACTION_COMPLETED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionFailedTopic() {
        return TopicBuilder.name(TRANSACTION_FAILED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transferCompletedTopic() {
        return TopicBuilder.name(TRANSFER_COMPLETED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transferFailedTopic() {
        return TopicBuilder.name(TRANSFER_FAILED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
