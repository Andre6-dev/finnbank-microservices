package com.finnova.gateway_server.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Configuration.
 * Configures routes for all microservices in the banking system.
 *
 * <p>Routes are configured to use Eureka service discovery for
 * dynamic service resolution and load balancing.</p>
 *
 * @author Andre Gallegos
 * @version 1.0.0
 */
@Configuration
public class GatewayConfig {

    /**
     * Configures route locator with all microservice routes.
     *
     * @param builder the route locator builder
     * @return configured route locator
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("authServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri("lb://AUTH-SERVICE"))

                // Customer Service Routes
                .route("customer-service", r -> r
                        .path("/api/customers/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("customerServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/customers")))
                        .uri("lb://CUSTOMER-SERVICE"))

                // Passive Product Service Routes
                .route("passive-product-service", r -> r
                        .path("/api/passive-products/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("passiveProductServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/passive-products")))
                        .uri("lb://PRODUCTS-SERVICE"))

                // Active Product Service Routes
                .route("active-product-service", r -> r
                        .path("/api/active-products/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("activeProductServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/active-products")))
                        .uri("lb://PRODUCTS-SERVICE"))

                // Transaction Service Routes
                .route("transaction-service", r -> r
                        .path("/api/transactions/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("transactionServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/transactions")))
                        .uri("lb://TRANSACTION-SERVICE"))

                // Debit Card Service Routes
                .route("debit-card-service", r -> r
                        .path("/api/debit-cards/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("debitCardServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/debit-cards")))
                        .uri("lb://DEBIT-CARD-SERVICE"))

                // Transfer Service Routes
                .route("transfer-service", r -> r
                        .path("/api/transfers/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("transferServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/transfers")))
                        .uri("lb://TRANSFER-SERVICE"))

                // Report Service Routes
                .route("report-service", r -> r
                        .path("/api/reports/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("reportServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/reports")))
                        .uri("lb://REPORT-SERVICE"))

                // Yanki Service Routes
                .route("yanki-service", r -> r
                        .path("/api/yanki/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("yankiServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/yanki")))
                        .uri("lb://YANKI-SERVICE"))

                // BootCoin Service Routes
                .route("bootcoin-service", r -> r
                        .path("/api/bootcoin/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("bootcoinServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/bootcoin")))
                        .uri("lb://BOOTCOIN-SERVICE"))

                .build();
    }
}
