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

@Document(collection = "bootcoin_wallets")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BootCoinWallet {

    @Id
    private String id;

    private String documentType;

    @Indexed(unique = true)
    private String documentNumber;

    private String phoneNumber;
    private String email;

    @Builder.Default
    private BigDecimal bootCoinBalance = BigDecimal.ZERO;

    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
