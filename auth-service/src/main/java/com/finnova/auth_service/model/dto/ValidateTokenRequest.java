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
public class ValidateTokenRequest {

    /**
     * JWT token to validate.
     */
    @NotBlank(message = "Token is required")
    private String token;
}
