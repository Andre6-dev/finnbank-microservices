package com.finnova.transaction_service.model.dto;

import com.finnova.transaction_service.model.enums.TransactionStatus;
import com.finnova.transaction_service.model.enums.TransactionType;
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
public class TransactionResponse {

    private String id;
    private String transactionNumber;
    private String customerId;
    private String productId;
    private String productType;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private BigDecimal commission;
    private String destinationProductId;
    private String destinationCustomerId;
    private String description;
    private TransactionStatus status;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
}
