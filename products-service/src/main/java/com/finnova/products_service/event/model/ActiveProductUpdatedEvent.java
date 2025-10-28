package com.finnova.products_service.event.model;

import com.finnova.products_service.model.enums.ProductStatus;
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
public class ActiveProductUpdatedEvent {

    private String productId;
    private String creditNumber;
    private String customerId;
    private BigDecimal availableCredit;
    private BigDecimal outstandingBalance;
    private ProductStatus status;
    private Boolean hasOverdueDebt;
    private LocalDateTime timestamp;
}
