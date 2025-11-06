package com.finnova.report_service.service;

import com.finnova.report_service.client.CardClient;
import com.finnova.report_service.client.CustomerClient;
import com.finnova.report_service.client.ProductClient;
import com.finnova.report_service.client.TransactionClient;
import com.finnova.report_service.model.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final TransactionClient transactionClient;
    private final CardClient cardClient;

    // ========== CONSOLIDATED REPORT ==========

    /**
     * Generate consolidated report for a customer (Proyecto III)
     */
    public Mono<ConsolidatedReportDto> getConsolidatedReport(String customerId) {
        log.info("Generating consolidated report for customer: {}", customerId);

        return Mono.zip(
                customerClient.getCustomer(customerId),
                productClient.getProductsByCustomer(customerId).collectList(),
                transactionClient.getTransactionsByCustomer(customerId)
                        .sort((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
                        .take(20)
                        .collectList(),
                cardClient.getDebitCardsByCustomer(customerId).collectList()
        ).flatMap(tuple -> {
            CustomerDto customer = tuple.getT1();
            List<ProductDto> products = tuple.getT2();
            List<TransactionDto> recentTransactions = tuple.getT3();
            List<DebitCardDto> debitCards = tuple.getT4();

            // Separate passive and active products
            List<ProductDto> passiveProducts = products.stream()
                    .filter(p -> isPassiveProduct(p.getProductType()))
                    .collect(Collectors.toList());

            List<ProductDto> activeProducts = products.stream()
                    .filter(p -> isActiveProduct(p.getProductType()))
                    .collect(Collectors.toList());

            // Convert to summaries
            return Flux.fromIterable(passiveProducts)
                    .flatMap(product -> countProductTransactions(product.getId())
                            .map(count -> ConsolidatedReportDto.ProductSummary.builder()
                                    .productId(product.getId())
                                    .productType(product.getProductType())
                                    .accountNumber(product.getAccountNumber())
                                    .balance(product.getBalance())
                                    .status(product.getStatus())
                                    .transactionCount(count)
                                    .build())
                    )
                    .collectList()
                    .flatMap(passiveSummaries ->
                            Flux.fromIterable(activeProducts)
                                    .flatMap(product -> countProductTransactions(product.getId())
                                            .map(count -> ConsolidatedReportDto.ProductSummary.builder()
                                                    .productId(product.getId())
                                                    .productType(product.getProductType())
                                                    .accountNumber(product.getAccountNumber())
                                                    .creditLimit(product.getCreditLimit())
                                                    .availableBalance(product.getAvailableBalance())
                                                    .balance(product.getCreditLimit().subtract(product.getAvailableBalance()))
                                                    .status(product.getStatus())
                                                    .transactionCount(count)
                                                    .build())
                                    )
                                    .collectList()
                                    .map(activeSummaries -> {
                                        // Calculate totals
                                        BigDecimal totalBalance = passiveSummaries.stream()
                                                .map(ConsolidatedReportDto.ProductSummary::getBalance)
                                                .filter(Objects::nonNull)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        BigDecimal totalAvailableCredit = activeSummaries.stream()
                                                .map(ConsolidatedReportDto.ProductSummary::getAvailableBalance)
                                                .filter(Objects::nonNull)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        BigDecimal totalDebt = activeSummaries.stream()
                                                .map(ConsolidatedReportDto.ProductSummary::getBalance)
                                                .filter(Objects::nonNull)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        BigDecimal netWorth = totalBalance.subtract(totalDebt);

                                        return ConsolidatedReportDto.builder()
                                                .customer(customer)
                                                .passiveProducts(passiveSummaries)
                                                .activeProducts(activeSummaries)
                                                .debitCards(debitCards)
                                                .recentTransactions(recentTransactions)
                                                .totalBalance(totalBalance)
                                                .totalAvailableCredit(totalAvailableCredit)
                                                .totalDebt(totalDebt)
                                                .netWorth(netWorth)
                                                .generatedAt(LocalDateTime.now())
                                                .build();
                                    })
                    );
        });
    }

    // ========== DAILY AVERAGE REPORT ==========

    /**
     * Generate daily average balance report (Proyecto II)
     */
    public Mono<DailyAverageReportDto> getDailyAverageBalanceReport(String customerId, Integer month, Integer year) {
        log.info("Generating daily average report for customer: {} - {}/{}", customerId, month, year);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        int daysInMonth = yearMonth.lengthOfMonth();

        return productClient.getProductsByCustomer(customerId)
                .filter(product -> isPassiveProduct(product.getProductType()) || isActiveProduct(product.getProductType()))
                .flatMap(product ->
                        transactionClient.getTransactionsByProductAndDateRange(product.getId(), startDate, endDate)
                                .collectList()
                                .map(transactions -> calculateDailyAverage(product, transactions, daysInMonth))
                )
                .collectMap(
                        avg -> avg.getProductId(),
                        avg -> avg
                )
                .map(productAverages -> {
                    BigDecimal overallAverage = productAverages.values().stream()
                            .map(DailyAverageReportDto.ProductDailyAverage::getAverageDailyBalance)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return DailyAverageReportDto.builder()
                            .customerId(customerId)
                            .month(month)
                            .year(year)
                            .productAverages(productAverages)
                            .overallAverageBalance(overallAverage)
                            .generatedAt(LocalDateTime.now())
                            .build();
                });
    }

    /**
     * Calculate daily average for a product
     */
    private DailyAverageReportDto.ProductDailyAverage calculateDailyAverage(
            ProductDto product,
            List<TransactionDto> transactions,
            int daysInMonth
    ) {
        if (transactions.isEmpty()) {
            return DailyAverageReportDto.ProductDailyAverage.builder()
                    .productId(product.getId())
                    .productType(product.getProductType())
                    .accountNumber(product.getAccountNumber())
                    .averageDailyBalance(product.getBalance())
                    .minBalance(product.getBalance())
                    .maxBalance(product.getBalance())
                    .daysInMonth(daysInMonth)
                    .build();
        }

        // Sort transactions by date
        List<TransactionDto> sortedTransactions = transactions.stream()
                .sorted(Comparator.comparing(TransactionDto::getTransactionDate))
                .collect(Collectors.toList());

        // Calculate daily balances
        BigDecimal sumOfDailyBalances = BigDecimal.ZERO;
        BigDecimal currentBalance = sortedTransactions.get(0).getBalanceBefore();
        BigDecimal minBalance = currentBalance;
        BigDecimal maxBalance = currentBalance;

        for (TransactionDto tx : sortedTransactions) {
            currentBalance = tx.getBalanceAfter();
            sumOfDailyBalances = sumOfDailyBalances.add(currentBalance);

            if (currentBalance.compareTo(minBalance) < 0) {
                minBalance = currentBalance;
            }
            if (currentBalance.compareTo(maxBalance) > 0) {
                maxBalance = currentBalance;
            }
        }

        // Calculate average
        BigDecimal averageDailyBalance = sumOfDailyBalances.divide(
                BigDecimal.valueOf(daysInMonth),
                2,
                RoundingMode.HALF_UP
        );

        return DailyAverageReportDto.ProductDailyAverage.builder()
                .productId(product.getId())
                .productType(product.getProductType())
                .accountNumber(product.getAccountNumber())
                .averageDailyBalance(averageDailyBalance)
                .minBalance(minBalance)
                .maxBalance(maxBalance)
                .daysInMonth(daysInMonth)
                .build();
    }

    // ========== COMMISSION REPORT ==========

    /**
     * Generate commission report for a product (Proyecto II)
     */
    public Mono<CommissionReportDto> getCommissionReport(
            String productId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.info("Generating commission report for product: {} from {} to {}", productId, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return productClient.getProduct(productId)
                .flatMap(product ->
                        transactionClient.getTransactionsByProductAndDateRange(productId, startDateTime, endDateTime)
                                .filter(tx -> tx.getCommission() != null && tx.getCommission().compareTo(BigDecimal.ZERO) > 0)
                                .map(tx -> CommissionReportDto.CommissionDetail.builder()
                                        .transactionId(tx.getId())
                                        .transactionNumber(tx.getTransactionNumber())
                                        .transactionDate(tx.getTransactionDate())
                                        .transactionType(tx.getTransactionType())
                                        .amount(tx.getAmount())
                                        .commission(tx.getCommission())
                                        .description(tx.getDescription())
                                        .build()
                                )
                                .collectList()
                                .map(commissions -> {
                                    BigDecimal totalCommissions = commissions.stream()
                                            .map(CommissionReportDto.CommissionDetail::getCommission)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                                    return CommissionReportDto.builder()
                                            .productId(product.getId())
                                            .productType(product.getProductType())
                                            .accountNumber(product.getAccountNumber())
                                            .startDate(startDate)
                                            .endDate(endDate)
                                            .commissions(commissions)
                                            .totalCommissions(totalCommissions)
                                            .totalTransactions(commissions.size())
                                            .generatedAt(LocalDateTime.now())
                                            .build();
                                })
                );
    }

    // ========== PRODUCT REPORT ==========

    /**
     * Generate general product report (Proyecto III)
     */
    public Mono<ProductReportDto> getProductReport(
            String productType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.info("Generating product report for type: {} from {} to {}", productType, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return productClient.getProductsByType(productType)
                .flatMap(product ->
                        transactionClient.getTransactionsByProductAndDateRange(product.getId(), startDateTime, endDateTime)
                                .collectList()
                                .map(transactions -> {
                                    BigDecimal totalCommissions = transactions.stream()
                                            .map(TransactionDto::getCommission)
                                            .filter(Objects::nonNull)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                                    return ProductReportDto.ProductStats.builder()
                                            .productId(product.getId())
                                            .accountNumber(product.getAccountNumber())
                                            .customerId(product.getCustomerId())
                                            .currentBalance(product.getBalance())
                                            .transactionCount(transactions.size())
                                            .totalCommissions(totalCommissions)
                                            .status(product.getStatus())
                                            .build();
                                })
                )
                .collectList()
                .map(productStats -> {
                    int totalProducts = productStats.size();
                    int activeProducts = (int) productStats.stream()
                            .filter(p -> "ACTIVE".equals(p.getStatus()))
                            .count();
                    int inactiveProducts = totalProducts - activeProducts;

                    BigDecimal totalBalance = productStats.stream()
                            .map(ProductReportDto.ProductStats::getCurrentBalance)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal averageBalance = totalProducts > 0
                            ? totalBalance.divide(BigDecimal.valueOf(totalProducts), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    int totalTransactions = productStats.stream()
                            .mapToInt(ProductReportDto.ProductStats::getTransactionCount)
                            .sum();

                    BigDecimal totalCommissions = productStats.stream()
                            .map(ProductReportDto.ProductStats::getTotalCommissions)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return ProductReportDto.builder()
                            .productType(productType)
                            .startDate(startDate)
                            .endDate(endDate)
                            .totalProducts(totalProducts)
                            .activeProducts(activeProducts)
                            .inactiveProducts(inactiveProducts)
                            .totalBalance(totalBalance)
                            .averageBalance(averageBalance)
                            .productStatistics(productStats)
                            .totalTransactions(totalTransactions)
                            .totalCommissions(totalCommissions)
                            .generatedAt(LocalDateTime.now())
                            .build();
                });
    }

    // ========== HELPER METHODS ==========

    private Mono<Integer> countProductTransactions(String productId) {
        return transactionClient.getTransactionsByProduct(productId)
                .count()
                .map(Long::intValue);
    }

    private boolean isPassiveProduct(String productType) {
        return "SAVINGS".equals(productType)
                || "CHECKING".equals(productType)
                || "FIXED_TERM".equals(productType);
    }

    private boolean isActiveProduct(String productType) {
        return "CREDIT".equals(productType)
                || "CREDIT_CARD".equals(productType);
    }
}
