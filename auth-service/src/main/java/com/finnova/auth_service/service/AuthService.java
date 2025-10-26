package com.finnova.auth_service.service;

import com.finnova.auth_service.model.dto.AuthResponse;
import com.finnova.auth_service.model.dto.LoginRequest;
import com.finnova.auth_service.model.dto.RegisterRequest;
import reactor.core.publisher.Mono;

public interface AuthService {

    /**
     * Registers a new user.
     *
     * @param request the registration request
     * @return Mono containing the authentication response with token
     */
    Mono<AuthResponse> register(RegisterRequest request);

    /**
     * Authenticates a user and generates a token.
     *
     * @param request the login request
     * @return Mono containing the authentication response with token
     */
    Mono<AuthResponse> login(LoginRequest request);

    /**
     * Validates a JWT token.
     *
     * @param token the token to validate
     * @return Mono of Boolean - true if valid, false otherwise
     */
    Mono<Boolean> validateToken(String token);

    /**
     * Refreshes a JWT token.
     *
     * @param token the current token
     * @return Mono containing the new authentication response with refreshed token
     */
    Mono<AuthResponse> refreshToken(String token);
}
