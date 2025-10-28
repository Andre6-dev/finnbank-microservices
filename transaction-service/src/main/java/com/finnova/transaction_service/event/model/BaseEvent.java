package com.finnova.transaction_service.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String source;
}
