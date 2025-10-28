package com.finnova.customer_service.model.dto;

import com.finnova.customer_service.model.enums.CustomerType;
import com.finnova.customer_service.model.enums.DocumentType;
import com.finnova.customer_service.model.enums.ProfileType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    @NotNull(message = "Customer type is required")
    private CustomerType customerType;

    @NotNull(message = "Profile type is required")
    private ProfileType profileType;

    private String firstName;

    private String lastName;

    private String businessName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String address;
}
