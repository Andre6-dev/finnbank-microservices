package com.finnova.products_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private String id;
    private String documentType;
    private String documentNumber;
    private String customerType;
    private String profileType;
    private String firstName;
    private String lastName;
    private String businessName;
    private String email;
    private String phone;
    private String address;
    private Boolean active;
}