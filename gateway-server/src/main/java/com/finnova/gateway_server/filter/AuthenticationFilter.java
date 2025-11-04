package com.finnova.gateway_server.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.finnova.gateway_server.util.JwtUtil;
import com.finnova.gateway_server.util.RouteValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Authentication Filter for API Gateway using Auth0 JWT.
 * Validates JWT tokens for secured endpoints.
 *
 * <p>This filter:
 * <ul>
 *   <li>Checks if the route requires authentication</li>
 *   <li>Extracts and validates JWT token from Authorization header</li>
 *   <li>Adds user information to request headers for downstream services</li>
 *   <li>Returns 401 Unauthorized for invalid tokens</li>
 *   <li>Supports role-based authorization</li>
 * </ul>
 * </p>
 *
 * @author NTT Data
 * @version 1.0.0
 */
@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USERNAME = "X-User-Username";
    private static final String HEADER_ROLES = "X-User-Roles";
    private static final String HEADER_USER_ID = "X-User-Id";

    /**
     * Constructor for AuthenticationFilter.
     */
    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Check if route is secured
            if (validator.isSecured.test(request)) {
                log.info("Secured route detected: {} {}",
                        request.getMethod(),
                        request.getURI().getPath());

                // Check if Authorization header exists
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    log.warn("Missing Authorization header for secured route: {}",
                            request.getURI().getPath());
                    return onError(exchange,
                            "Missing Authorization header",
                            HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                // Validate Bearer token format
                if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                    log.warn("Invalid Authorization header format for route: {}",
                            request.getURI().getPath());
                    return onError(exchange,
                            "Invalid Authorization header format. Expected: Bearer <token>",
                            HttpStatus.UNAUTHORIZED);
                }

                // Extract token
                String token = authHeader.substring(BEARER_PREFIX.length());

                try {
                    // Validate token using Auth0 JWT
                    if (!jwtUtil.validateToken(token)) {
                        log.warn("Invalid or expired JWT token for route: {}",
                                request.getURI().getPath());
                        return onError(exchange,
                                "Invalid or expired token",
                                HttpStatus.UNAUTHORIZED);
                    }

                    // Extract username from token
                    String username = jwtUtil.extractUsername(token);
                    if (username == null || username.isEmpty()) {
                        log.warn("Token does not contain valid username");
                        return onError(exchange,
                                "Invalid token: missing username",
                                HttpStatus.UNAUTHORIZED);
                    }

                    log.info("Authenticated user: {} for route: {}",
                            username,
                            request.getURI().getPath());

                    // Extract additional claims
                    String userId = jwtUtil.extractClaim(token, "userId");
                    String[] roles = jwtUtil.extractRoles(token);

                    // Log token expiration info
                    long remainingSeconds = jwtUtil.getRemainingValidityInSeconds(token);
                    log.debug("Token remaining validity: {} seconds", remainingSeconds);

                    // Build new request with user information in headers
                    ServerHttpRequest.Builder mutatedRequest = exchange.getRequest().mutate()
                            .header(HEADER_USERNAME, username);

                    if (userId != null && !userId.isEmpty()) {
                        mutatedRequest.header(HEADER_USER_ID, userId);
                    }

                    if (roles != null && roles.length > 0) {
                        mutatedRequest.header(HEADER_ROLES, String.join(",", roles));
                        log.debug("User roles: {}", String.join(",", roles));
                    }

                    request = mutatedRequest.build();

                } catch (JWTVerificationException e) {
                    log.error("JWT verification failed for route {}: {}",
                            request.getURI().getPath(),
                            e.getMessage());
                    return onError(exchange,
                            "Token verification failed: " + e.getMessage(),
                            HttpStatus.UNAUTHORIZED);
                } catch (Exception e) {
                    log.error("Unexpected error during token validation for route {}: {}",
                            request.getURI().getPath(),
                            e.getMessage(), e);
                    return onError(exchange,
                            "Token validation error",
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                log.info("Public route accessed: {} {}",
                        request.getMethod(),
                        request.getURI().getPath());
            }

            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    /**
     * Handles error responses with detailed JSON error message.
     *
     * @param exchange the server web exchange
     * @param errorMessage the error message
     * @param httpStatus the HTTP status
     * @return a Mono of Void
     */
    private Mono<Void> onError(ServerWebExchange exchange,
                               String errorMessage,
                               HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Create detailed error response
        String errorResponse = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                Instant.now().toString(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                errorMessage,
                exchange.getRequest().getURI().getPath()
        );

        byte[] bytes = errorResponse.getBytes(StandardCharsets.UTF_8);

        log.error("Authentication error - Status: {}, Message: {}, Path: {}",
                httpStatus.value(),
                errorMessage,
                exchange.getRequest().getURI().getPath());

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    /**
     * Configuration class for the filter.
     * Can be extended to add custom filter configuration.
     */
    public static class Config {
        // Configuration properties if needed in the future
        // For example: required roles, custom claim validations, etc.

        private boolean validateRoles = false;
        private String[] requiredRoles = new String[0];

        public boolean isValidateRoles() {
            return validateRoles;
        }

        public void setValidateRoles(boolean validateRoles) {
            this.validateRoles = validateRoles;
        }

        public String[] getRequiredRoles() {
            return requiredRoles;
        }

        public void setRequiredRoles(String[] requiredRoles) {
            this.requiredRoles = requiredRoles;
        }
    }
}
