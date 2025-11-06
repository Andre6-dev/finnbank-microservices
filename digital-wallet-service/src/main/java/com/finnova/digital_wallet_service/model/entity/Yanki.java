package com.finnova.digital_wallet_service.model.entity;

import com.finnova.digital_wallet_service.model.enums.WalletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "yankis")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Yanki {

    @Id
    private String id;

    private String documentType; // DNI, CEX, PASSPORT

    @Indexed(unique = true)
    private String documentNumber;

    @Indexed(unique = true)
    private String phoneNumber;

    private String imei;
    private String email;

    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    private String currency; // e.g., USD, EUR
    private String associatedDebitCardId; // Optional

    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
