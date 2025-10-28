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
public class PassiveProductUpdatedEvent {

    private String productId;
    private String accountNumber;
    private String customerId;
    private BigDecimal balance;
    private ProductStatus status;
    private LocalDateTime timestamp;
}
