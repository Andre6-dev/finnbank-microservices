package com.finnova.digital_wallet_service.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitCardDto {
    private String id;
    private String cardNumber;
    private String customerId;
}
