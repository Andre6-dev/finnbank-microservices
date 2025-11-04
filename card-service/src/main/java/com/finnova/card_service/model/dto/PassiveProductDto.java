package com.finnova.card_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassiveProductDto {

    private String id;
    private String accountNumber;
    private String customerId;
    private String productType; // SAVINGS, CHECKING, FIXED_TERM
    private BigDecimal balance;
    private String currency;
    private BigDecimal openingAmount;
    private BigDecimal maintenanceFee;
    private Integer maxTransactionsWithoutFee;
    private Integer currentMonthTransactions;
    private BigDecimal feePerExtraTransaction;
    private Integer movementDay;
    private BigDecimal minimumDailyAverage;
    private String status;
    private List<String> holders;
    private List<String> authorizedSigners;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

