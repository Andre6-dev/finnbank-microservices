package com.finnova.digital_wallet_service.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateYankiRequest {

    @NotBlank(message = "Document type is required")
    private String documentType;

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "IMEI is required")
    private String imei;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
