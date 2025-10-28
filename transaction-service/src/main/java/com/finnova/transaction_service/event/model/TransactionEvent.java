package com.finnova.transaction_service.event.model;

import com.finnova.transaction_service.model.enums.TransactionStatus;
import com.finnova.transaction_service.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String source;
    private String transactionId;
    private String transactionNumber;
    private String customerId;
    private String productId;
    private String productType;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private BigDecimal commission;
    private String description;
    private TransactionStatus status;
    private LocalDateTime transactionDate;
}
