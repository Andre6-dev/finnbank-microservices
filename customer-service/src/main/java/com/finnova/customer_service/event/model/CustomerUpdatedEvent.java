package com.finnova.customer_service.event.model;

import com.finnova.customer_service.model.enums.ProfileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdatedEvent {

    private String customerId;
    private String documentNumber;
    private ProfileType profileType;
    private String email;
    private Boolean active;
    private LocalDateTime timestamp;
}
