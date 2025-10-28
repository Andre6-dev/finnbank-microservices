package com.finnova.products_service.model.dto;

import com.finnova.products_service.model.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateActiveProductRequest {

    private BigDecimal creditLimit;
    private BigDecimal interestRate;
    private Integer paymentDueDate;
    private ProductStatus status;
}
