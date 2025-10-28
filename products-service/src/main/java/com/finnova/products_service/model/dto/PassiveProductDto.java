package com.finnova.products_service.model.dto;

import com.finnova.products_service.model.enums.Currency;
import com.finnova.products_service.model.enums.PassiveProductType;
import com.finnova.products_service.model.enums.ProductStatus;
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
    private PassiveProductType productType;
    private BigDecimal balance;
    private Currency currency;
    private BigDecimal openingAmount;
    private BigDecimal maintenanceFee;
    private Integer maxTransactionsWithoutFee;
    private Integer currentMonthTransactions;
    private BigDecimal feePerExtraTransaction;
    private Integer movementDay;
    private BigDecimal minimumDailyAverage;
    private ProductStatus status;
    private List<String> holders;
    private List<String> authorizedSigners;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
