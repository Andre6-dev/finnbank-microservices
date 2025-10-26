package com.finnova.gateway_server.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Logging Filter for API Gateway.
 * Logs incoming requests and outgoing responses.
 *
 * @author Andre Gallegos
 * @version 1.0.0
 */
@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        log.info("Incoming request: {} {}",
                request.getMethod(),
                request.getURI().getPath());

        log.debug("Request headers: {}", request.getHeaders());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            log.info("Outgoing response: {} - Status: {}",
                    request.getURI().getPath(),
                    response.getStatusCode());
        }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
