package com.finnova.transaction_service.event.model;

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
public class TransferEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String source;
    private String transferId;
    private String transactionNumber;
    private String sourceCustomerId;
    private String sourceProductId;
    private String destinationCustomerId;
    private String destinationProductId;
    private BigDecimal amount;
    private String transferType; // OWN_ACCOUNTS, THIRD_PARTY
    private String description;
    private String status;
}
