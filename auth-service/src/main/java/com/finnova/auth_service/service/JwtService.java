package com.finnova.auth_service.service;

import com.finnova.auth_service.model.entity.User;
import reactor.core.publisher.Mono;

public interface JwtService {

    /**
     * Generates a JWT token for the given user.
     *
     * @param user the user for whom to generate the token
     * @return Mono containing the JWT token
     */
    Mono<String> generateToken(User user);

    /**
     * Validates a JWT token.
     *
     * @param token the token to validate
     * @return Mono of Boolean - true if valid, false otherwise
     */
    Mono<Boolean> validateToken(String token);

    /**
     * Extracts the username from a JWT token.
     *
     * @param token the JWT token
     * @return Mono containing the username
     */
    Mono<String> extractUsername(String token);

    /**
     * Gets the token expiration time in milliseconds.
     *
     * @return the expiration time
     */
    Long getExpirationTime();
}
