package com.finnova.transaction_service.model.dto;

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
public class ActiveProductDto {

    private String id;
    private String creditNumber;
    private String customerId;
    private String productType; // CREDIT, CREDIT_CARD
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;
    private BigDecimal usedCredit;
    private BigDecimal interestRate;
    private String currency;
    private Integer paymentDueDate;
    private BigDecimal minimumPayment;
    private BigDecimal outstandingBalance;
    private BigDecimal overdueAmount;
    private Boolean hasOverdueDebt;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

