package com.finnova.card_service.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitCardEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String source;

    private String cardId;
    private String cardNumber;
    private String customerId;
    private String accountId; // For account association events
    private String status;
}
