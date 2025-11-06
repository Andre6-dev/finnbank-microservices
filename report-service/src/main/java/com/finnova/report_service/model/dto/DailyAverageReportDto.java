package com.finnova.report_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Daily average balance report for a customer's products
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyAverageReportDto {

    private String customerId;
    private Integer month;
    private Integer year;

    // Average balances per product
    private Map<String, ProductDailyAverage> productAverages;

    // Overall average
    private BigDecimal overallAverageBalance;

    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDailyAverage {
        private String productId;
        private String productType;
        private String accountNumber;
        private BigDecimal averageDailyBalance;
        private BigDecimal minBalance;
        private BigDecimal maxBalance;
        private Integer daysInMonth;
    }
}
