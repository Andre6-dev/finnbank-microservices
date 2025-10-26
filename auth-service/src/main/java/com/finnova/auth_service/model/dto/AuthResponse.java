package com.finnova.auth_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * JWT access token.
     */
    private String token;

    /**
     * Token type (usually "Bearer").
     */
    private String tokenType;

    /**
     * Username of the authenticated user.
     */
    private String username;

    /**
     * Email of the authenticated user.
     */
    private String email;

    /**
     * Roles of the authenticated user.
     */
    private List<String> roles;

    /**
     * Token expiration time in milliseconds.
     */
    private Long expiresIn;
}
