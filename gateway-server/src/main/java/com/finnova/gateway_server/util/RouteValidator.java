package com.finnova.gateway_server.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * Route Validator utility.
 * Determines which routes are public (don't require authentication).
 *
 * @author Andre Gallegos
 * @version 1.0.0
 */
@Component
public class RouteValidator {

    /**
     * List of open/public API endpoints that don't require authentication.
     */
    public static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/validate",
            "/actuator/**",
            "/eureka/**"
    );

    /**
     * Predicate to check if the request is secured (requires authentication).
     */
    public Predicate<ServerHttpRequest> isSecured =
            request -> OPEN_API_ENDPOINTS
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
