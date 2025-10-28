package com.finnova.products_service.event.model;

import com.finnova.products_service.model.enums.ActiveProductType;
import com.finnova.products_service.model.enums.Currency;
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
public class ActiveProductCreatedEvent {

    private String productId;
    private String creditNumber;
    private String customerId;
    private ActiveProductType productType;
    private BigDecimal creditLimit;
    private Currency currency;
    private LocalDateTime timestamp;
}
