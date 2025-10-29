package com.finnova.digital_wallet_service.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptBuyRequestDto {

    @NotBlank(message = "Seller wallet ID is required")
    private String sellerWalletId;
}
