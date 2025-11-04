package com.finnova.card_service.model.entity;

import com.finnova.card_service.model.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "debit_cards")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DebitCard {

    @Id
    private String id;

    @Indexed(unique = true)
    private String cardNumber;

    @Indexed
    private String customerId;
    private String mainAccountId;

    @Builder.Default
    private List<String> associatedAccountIds = new ArrayList<>();

    private String cardType;
    private LocalDate expirationDate;
    private String cvv;
    private String cardHolderName;

    @Builder.Default
    private CardStatus status = CardStatus.ACTIVE;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
