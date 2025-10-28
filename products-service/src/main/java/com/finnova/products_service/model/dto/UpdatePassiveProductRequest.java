package com.finnova.products_service.model.dto;

import com.finnova.products_service.model.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePassiveProductRequest {

    private BigDecimal maintenanceFee;
    private Integer maxTransactionsWithoutFee;
    private BigDecimal feePerExtraTransaction;
    private BigDecimal minimumDailyAverage;
    private ProductStatus status;
    private List<String> holders;
    private List<String> authorizedSigners;
}
