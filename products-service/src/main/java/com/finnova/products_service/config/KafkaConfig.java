package com.finnova.products_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Value("${spring.kafka.topic.passive-product-events}")
    private String passiveProductEventsTopic;

    @Value("${spring.kafka.topic.active-product-events}")
    private String activeProductEventsTopic;

    @Value("${spring.kafka.topic.balance-events}")
    private String balanceEventsTopic;

    @Value("${spring.kafka.topic.customer-events}")
    private String customerEventsTopic;
}
