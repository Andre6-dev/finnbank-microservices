package com.finnova.report_service.model.dto;

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
public class TransactionDto {
    private String id;
    private String transactionNumber;
    private String customerId;
    private String productId;
    private String productType;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private BigDecimal commission;
    private String destinationProductId;
    private String destinationCustomerId;
    private String description;
    private String status;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
}
