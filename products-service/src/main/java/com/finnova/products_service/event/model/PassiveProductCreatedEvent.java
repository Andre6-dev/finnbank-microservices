package com.finnova.products_service.event.model;

import com.finnova.products_service.model.enums.Currency;
import com.finnova.products_service.model.enums.PassiveProductType;
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
public class PassiveProductCreatedEvent {

    private String productId;
    private String accountNumber;
    private String customerId;
    private PassiveProductType productType;
    private BigDecimal balance;
    private Currency currency;
    private LocalDateTime timestamp;
}
