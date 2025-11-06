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
public class ProductDto {
    private String id;
    private String customerId;
    private String productType;
    private String accountNumber;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private BigDecimal availableBalance;
    private String status;
    private Integer monthlyTransactionCount;
    private Integer maxFreeTransactions;
    private LocalDateTime createdAt;
}
