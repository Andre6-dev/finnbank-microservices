package com.finnova.digital_wallet_service.model.entity;

import com.finnova.digital_wallet_service.model.enums.PaymentMethod;
import com.finnova.digital_wallet_service.model.enums.TransactionStatus;
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

@Document(collection = "bootcoin_transactions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BootCoinTransaction {

    @Id
    private String id;

    @Indexed(unique = true)
    private String transactionNumber;

    private String buyerWalletId;
    private String sellerWalletId;

    private BigDecimal solesAmount;
    private BigDecimal bootCoinAmount;
    private BigDecimal exchangeRate;

    private PaymentMethod paymentMethod; // YANKI, BANK_TRANSFER
    private String paymentDetails; // Account number or phone number

    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Indexed
    private LocalDateTime transactionDate;
    private LocalDateTime acceptedDate;
    private LocalDateTime completedDate;
}