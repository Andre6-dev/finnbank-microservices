package com.finnova.gateway_server.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Global Exception Handler for API Gateway.
 * Handles exceptions and returns appropriate error responses.
 *
 * @author Andre Gallegos
 * @version 1.0.0
 */
@Component
@Order(-1)
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Error occurred: ", ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "An unexpected error occurred";

        if (ex instanceof UnauthorizedException) {
            status = HttpStatus.UNAUTHORIZED;
            message = ex.getMessage();
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorResponse = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                java.time.Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getURI().getPath()
        );

        DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
        byte[] bytes = errorResponse.getBytes(StandardCharsets.UTF_8);

        return exchange.getResponse().writeWith(
                Mono.just(dataBufferFactory.wrap(bytes))
        );
    }
}
