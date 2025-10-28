package com.finnova.products_service.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceChangedEvent {

    private String productId;
    private String productNumber; // accountNumber or creditNumber
    private String customerId;
    private String operationType; // DEPOSIT, WITHDRAWAL, CHARGE, PAYMENT
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private BigDecimal amount;
    private String description;
    private LocalDateTime timestamp;
}
