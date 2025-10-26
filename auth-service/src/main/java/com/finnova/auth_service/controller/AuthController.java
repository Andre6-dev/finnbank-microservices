package com.finnova.auth_service.controller;

import com.finnova.auth_service.model.dto.AuthResponse;
import com.finnova.auth_service.model.dto.LoginRequest;
import com.finnova.auth_service.model.dto.RegisterRequest;
import com.finnova.auth_service.model.dto.ValidateTokenRequest;
import com.finnova.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user.
     *
     * @param request the registration request
     * @return Mono of ResponseEntity with authentication response
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /auth/register - Registering user: {}", request.getUsername());
        return authService.register(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .doOnSuccess(r -> log.info("User registered successfully: {}", request.getUsername()))
                .doOnError(e -> log.error("Error registering user: {}", e.getMessage()));
    }

    /**
     * Authenticates a user.
     *
     * @param request the login request
     * @return Mono of ResponseEntity with authentication response
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/login - Authenticating user: {}", request.getUsername());
        return authService.login(request)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("User authenticated successfully: {}", request.getUsername()))
                .doOnError(e -> log.error("Error authenticating user: {}", e.getMessage()));
    }

    /**
     * Validates a JWT token.
     *
     * @param request the validate token request
     * @return Mono of ResponseEntity with validation result
     */
    @PostMapping("/validate")
    public Mono<ResponseEntity<Map<String, Boolean>>> validateToken(
            @Valid @RequestBody ValidateTokenRequest request) {
        log.info("POST /auth/validate - Validating token");
        return authService.validateToken(request.getToken())
                .map(isValid -> ResponseEntity.ok(Map.of("valid", isValid)))
                .doOnSuccess(r -> log.info("Token validation completed"))
                .doOnError(e -> log.error("Error validating token: {}", e.getMessage()));
    }

    /**
     * Refreshes a JWT token.
     *
     * @param authorizationHeader the Authorization header with Bearer token
     * @return Mono of ResponseEntity with new authentication response
     */
    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(
            @RequestHeader("Authorization") String authorizationHeader) {
        log.info("POST /auth/refresh - Refreshing token");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        String token = authorizationHeader.substring(7);
        return authService.refreshToken(token)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Token refreshed successfully"))
                .doOnError(e -> log.error("Error refreshing token: {}", e.getMessage()));
    }
}
