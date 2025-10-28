package com.finnova.products_service.model.dto;

import com.finnova.products_service.model.enums.Currency;
import com.finnova.products_service.model.enums.PassiveProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreatePassiveProductRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Product type is required")
    private PassiveProductType productType;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Opening amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Opening amount must be non-negative")
    private BigDecimal openingAmount;

    private BigDecimal maintenanceFee;

    @Min(value = 0, message = "Max transactions must be non-negative")
    private Integer maxTransactionsWithoutFee;

    private BigDecimal feePerExtraTransaction;

    @Min(value = 1, message = "Movement day must be between 1 and 31")
    private Integer movementDay;

    private BigDecimal minimumDailyAverage;

    private List<String> holders;

    private List<String> authorizedSigners;
}
