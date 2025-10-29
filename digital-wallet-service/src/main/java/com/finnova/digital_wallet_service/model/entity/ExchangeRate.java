package com.finnova.digital_wallet_service.model.entity;

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

@Document(collection = "exchange_rates")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExchangeRate {

    @Id
    private String id;

    private BigDecimal buyRate;  // Soles to BootCoin
    private BigDecimal sellRate; // BootCoin to Soles

    @Indexed
    private LocalDateTime effectiveDate;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;
}
