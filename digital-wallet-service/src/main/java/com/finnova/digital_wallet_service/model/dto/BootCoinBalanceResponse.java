package com.finnova.digital_wallet_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BootCoinBalanceResponse {

    private String walletId;
    private BigDecimal bootCoinBalance;
}
