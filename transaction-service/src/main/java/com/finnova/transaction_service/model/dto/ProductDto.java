package com.finnova.transaction_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private String id;
    private String customerId;
    private String productType; // SAVINGS, CHECKING, FIXED_TERM, CREDIT, CREDIT_CARD
    private String accountNumber;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private BigDecimal availableBalance;
    private String currency;
    private String status;
    private Integer monthlyTransactionCount; // Para calcular comisiones
    private Integer maxFreeTransactions; // Transacciones sin comisión
}
