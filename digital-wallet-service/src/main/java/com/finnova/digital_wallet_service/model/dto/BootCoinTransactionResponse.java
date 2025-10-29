package com.finnova.digital_wallet_service.model.dto;

import com.finnova.digital_wallet_service.model.enums.PaymentMethod;
import com.finnova.digital_wallet_service.model.enums.TransactionStatus;
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
public class BootCoinTransactionResponse {

    private String id;
    private String transactionNumber;
    private String buyerWalletId;
    private String sellerWalletId;
    private BigDecimal solesAmount;
    private BigDecimal bootCoinAmount;
    private BigDecimal exchangeRate;
    private PaymentMethod paymentMethod;
    private String paymentDetails;
    private TransactionStatus status;
    private LocalDateTime transactionDate;
    private LocalDateTime acceptedDate;
    private LocalDateTime completedDate;
}
