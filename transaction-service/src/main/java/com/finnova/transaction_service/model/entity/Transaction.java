package com.finnova.transaction_service.model.entity;

import com.finnova.transaction_service.model.enums.TransactionStatus;
import com.finnova.transaction_service.model.enums.TransactionType;
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

@Document(collection = "transactions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Transaction {

    @Id
    private String id;

    @Indexed(unique = true)
    private String transactionNumber;

    @Indexed
    private String customerId;

    @Indexed
    private String productId; // accountId or creditId

    private String productType; // SAVINGS, CHECKING, FIXED_TERM, CREDIT, CREDIT_CARD

    private TransactionType transactionType; // DEPOSIT, WITHDRAWAL, PAYMENT, etc.

    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private BigDecimal commission;

    // For transfers
    private String destinationProductId;
    private String destinationCustomerId;

    private String description;
    private TransactionStatus status;

    @Indexed
    private LocalDateTime transactionDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
