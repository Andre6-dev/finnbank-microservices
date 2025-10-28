package com.finnova.products_service.model.dto;

import com.finnova.products_service.model.enums.ActiveProductType;
import com.finnova.products_service.model.enums.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateActiveProductRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Product type is required")
    private ActiveProductType productType;

    @NotNull(message = "Credit limit is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Credit limit must be positive")
    private BigDecimal creditLimit;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", message = "Interest rate must be non-negative")
    @DecimalMin(value = "100.0", message = "Interest rate cannot exceed 100%")
    private BigDecimal interestRate;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @Min(value = 1, message = "Payment due date must be between 1 and 31")
    @Max(value = 31, message = "Payment due date must be between 1 and 31")
    private Integer paymentDueDate;
}
