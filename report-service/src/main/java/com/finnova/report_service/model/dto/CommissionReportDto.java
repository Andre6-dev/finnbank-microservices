package com.finnova.report_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionReportDto {

    private String productId;
    private String productType;
    private String accountNumber;
    private LocalDate startDate;
    private LocalDate endDate;

    private List<CommissionDetail> commissions;

    private BigDecimal totalCommissions;
    private Integer totalTransactions;

    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommissionDetail {
        private String transactionId;
        private String transactionNumber;
        private LocalDateTime transactionDate;
        private String transactionType;
        private BigDecimal amount;
        private BigDecimal commission;
        private String description;
    }
}
