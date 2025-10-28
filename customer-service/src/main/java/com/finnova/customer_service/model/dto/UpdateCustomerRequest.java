package com.finnova.customer_service.model.dto;

import com.finnova.customer_service.model.enums.ProfileType;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {

    private ProfileType profileType;

    @Email(message = "Email must be valid")
    private String email;

    private String phone;

    private String address;

    private Boolean active;

}
