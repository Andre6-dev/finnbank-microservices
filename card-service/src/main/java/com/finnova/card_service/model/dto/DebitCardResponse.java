package com.finnova.card_service.model.dto;

import com.finnova.card_service.model.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitCardResponse {

    private String id;
    private String cardNumber;
    private String customerId;
    private List<String> associatedAccountIds;
    private String mainAccountId;
    private String cvv;
    private LocalDate expirationDate;
    private String cardholderName;
    private CardStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
