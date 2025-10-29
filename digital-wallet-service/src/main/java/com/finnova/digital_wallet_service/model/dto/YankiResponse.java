package com.finnova.digital_wallet_service.model.dto;

import com.finnova.digital_wallet_service.model.enums.WalletStatus;
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
public class YankiResponse {

    private String id;
    private String documentType;
    private String documentNumber;
    private String phoneNumber;
    private String imei;
    private String email;
    private BigDecimal balance;
    private String associatedDebitCardId;
    private WalletStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
