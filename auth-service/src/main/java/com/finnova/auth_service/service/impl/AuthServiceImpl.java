package com.finnova.auth_service.service.impl;

import com.finnova.auth_service.exception.InvalidCredentialsException;
import com.finnova.auth_service.exception.UserAlreadyExistsException;
import com.finnova.auth_service.exception.UserNotFoundException;
import com.finnova.auth_service.mapper.UserMapper;
import com.finnova.auth_service.model.dto.AuthResponse;
import com.finnova.auth_service.model.dto.LoginRequest;
import com.finnova.auth_service.model.dto.RegisterRequest;
import com.finnova.auth_service.model.entity.User;
import com.finnova.auth_service.repository.UserRepository;
import com.finnova.auth_service.service.AuthService;
import com.finnova.auth_service.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Mono<AuthResponse> register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        return userRepository.existsByUsername(request.getUsername())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.warn("Username already exists: {}", request.getUsername());
                        return Mono.error(new UserAlreadyExistsException(
                                "Username already exists: " + request.getUsername()));
                    }
                    return userRepository.existsByEmail(request.getEmail());
                })
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.warn("Email already exists: {}", request.getEmail());
                        return Mono.error(new UserAlreadyExistsException(
                                "Email already exists: " + request.getEmail()));
                    }

                    // Create user entity
                    User user = userMapper.toEntity(request);
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setActive(true);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());

                    // Set default role if none provided
                    if (request.getRoles() == null || request.getRoles().isEmpty()) {
                        user.setRoles(List.of("USER"));
                    } else {
                        user.setRoles(request.getRoles());
                    }

                    return userRepository.save(user);
                })
                .flatMap(savedUser -> {
                    log.info("User registered successfully: {}", savedUser.getUsername());
                    return jwtService.generateToken(savedUser)
                            .map(token -> buildAuthResponse(token, savedUser));
                });
    }

    @Override
    public Mono<AuthResponse> login(LoginRequest request) {
        log.info("Authenticating user: {}", request.getUsername());

        return userRepository.findByUsername(request.getUsername())
                .switchIfEmpty(Mono.error(new UserNotFoundException(
                        "User not found: " + request.getUsername())))
                .flatMap(user -> {
                    if (!user.getActive()) {
                        log.warn("User account is inactive: {}", request.getUsername());
                        return Mono.error(new InvalidCredentialsException(
                                "User account is inactive"));
                    }

                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        log.warn("Invalid password for user: {}", request.getUsername());
                        return Mono.error(new InvalidCredentialsException(
                                "Invalid credentials"));
                    }

                    log.info("User authenticated successfully: {}", user.getUsername());
                    return jwtService.generateToken(user)
                            .map(token -> buildAuthResponse(token, user));
                });
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        log.debug("Validating token");
        return jwtService.validateToken(token);
    }

    @Override
    public Mono<AuthResponse> refreshToken(String token) {
        log.info("Refreshing token");

        return jwtService.validateToken(token)
                .flatMap(isValid -> {
                    if (Boolean.FALSE.equals(isValid)) {
                        return Mono.error(new InvalidCredentialsException(
                                "Invalid or expired token"));
                    }
                    return jwtService.extractUsername(token);
                })
                .flatMap(username -> userRepository.findByUsername(username)
                        .switchIfEmpty(Mono.error(new UserNotFoundException(
                                "User not found: " + username))))
                .flatMap(user -> {
                    if (!user.getActive()) {
                        return Mono.error(new InvalidCredentialsException(
                                "User account is inactive"));
                    }

                    log.info("Token refreshed successfully for user: {}", user.getUsername());
                    return jwtService.generateToken(user)
                            .map(newToken -> buildAuthResponse(newToken, user));
                });
    }

    /**
     * Builds an authentication response from a token and user.
     *
     * @param token the JWT token
     * @param user the user entity
     * @return the authentication response
     */
    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }
}
