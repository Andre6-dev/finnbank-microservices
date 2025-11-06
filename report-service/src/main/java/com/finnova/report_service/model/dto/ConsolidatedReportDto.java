package com.finnova.report_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsolidatedReportDto {

    // Customer information
    private CustomerDto customer;

    // Products summary
    private List<ProductSummary> passiveProducts; // Accounts
    private List<ProductSummary> activeProducts;  // Credits
    private List<DebitCardDto> debitCards;

    // Recent transactions
    private List<TransactionDto> recentTransactions;

    // Totals
    private BigDecimal totalBalance;           // Total in accounts
    private BigDecimal totalAvailableCredit;   // Total available credit
    private BigDecimal totalDebt;              // Total debt
    private BigDecimal netWorth;               // Balance - Debt

    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSummary {
        private String productId;
        private String productType;
        private String accountNumber;
        private BigDecimal balance;
        private BigDecimal creditLimit;
        private BigDecimal availableBalance;
        private String status;
        private Integer transactionCount;
    }
}
