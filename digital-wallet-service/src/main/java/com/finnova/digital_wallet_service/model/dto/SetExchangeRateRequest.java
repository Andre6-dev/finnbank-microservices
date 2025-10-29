package com.finnova.digital_wallet_service.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetExchangeRateRequest {

    @NotNull(message = "Buy rate is required")
    @Positive(message = "Buy rate must be positive")
    private BigDecimal buyRate;

    @NotNull(message = "Sell rate is required")
    @Positive(message = "Sell rate must be positive")
    private BigDecimal sellRate;
}
