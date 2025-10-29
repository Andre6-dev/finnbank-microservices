package com.finnova.digital_wallet_service.model.dto;

import com.finnova.digital_wallet_service.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
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
public class CreateBuyRequestDto {
    @NotBlank(message = "Buyer wallet ID is required")
    private String buyerWalletId;

    @NotNull(message = "Amount in soles is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal solesAmount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "Payment details are required")
    private String paymentDetails; // Phone number or account number

}
