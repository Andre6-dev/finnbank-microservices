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
public class ProductReportDto {

    private String productType;
    private LocalDate startDate;
    private LocalDate endDate;

    private Integer totalProducts;
    private Integer activeProducts;
    private Integer inactiveProducts;

    private BigDecimal totalBalance;
    private BigDecimal averageBalance;

    private List<ProductStats> productStatistics;

    private Integer totalTransactions;
    private BigDecimal totalTransactionAmount;
    private BigDecimal totalCommissions;

    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductStats {
        private String productId;
        private String accountNumber;
        private String customerId;
        private BigDecimal currentBalance;
        private Integer transactionCount;
        private BigDecimal totalCommissions;
        private String status;
    }
}
