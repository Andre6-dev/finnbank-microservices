package com.finnova.auth_service.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    /**
     * Username for authentication.
     */
    @NotBlank(message = "Username is required")
    private String username;

    /**
     * Password for authentication.
     */
    @NotBlank(message = "Password is required")
    private String password;
}
