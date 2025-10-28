package com.finnova.products_service.model.dto;

import com.finnova.products_service.model.enums.ActiveProductType;
import com.finnova.products_service.model.enums.Currency;
import com.finnova.products_service.model.enums.ProductStatus;
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
    private ActiveProductType productType;
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;
    private BigDecimal usedCredit;
    private BigDecimal interestRate;
    private Currency currency;
    private Integer paymentDueDate;
    private BigDecimal minimumPayment;
    private BigDecimal outstandingBalance;
    private BigDecimal overdueAmount;
    private Boolean hasOverdueDebt;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
