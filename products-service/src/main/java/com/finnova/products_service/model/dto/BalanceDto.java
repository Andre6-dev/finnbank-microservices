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
public class BalanceDto {

    private String productId;
    private BigDecimal balance;
    private Currency currency;
    private BigDecimal availableBalance;
}
