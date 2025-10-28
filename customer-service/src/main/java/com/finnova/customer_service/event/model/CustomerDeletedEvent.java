package com.finnova.customer_service.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDeletedEvent {

    private String customerId;
    private String documentNumber;
    private LocalDateTime timestamp;
}
