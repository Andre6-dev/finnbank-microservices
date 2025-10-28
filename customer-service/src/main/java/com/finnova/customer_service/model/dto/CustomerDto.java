package com.finnova.customer_service.model.dto;

import com.finnova.customer_service.model.enums.CustomerType;
import com.finnova.customer_service.model.enums.DocumentType;
import com.finnova.customer_service.model.enums.ProfileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    private String id;
    private DocumentType documentType;
    private String documentNumber;
    private CustomerType customerType;
    private ProfileType profileType;
    private String firstName;
    private String lastName;
    private String businessName;
    private String email;
    private String phone;
    private String address;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
