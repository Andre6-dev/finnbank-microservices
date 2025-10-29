package com.finnova.digital_wallet_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BalanceResponse {

    private String walletId;
    private BigDecimal balance;
}
