package com.finnova.products_service.model.entity;

import com.finnova.products_service.model.enums.ActiveProductType;
import com.finnova.products_service.model.enums.Currency;
import com.finnova.products_service.model.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "active_products")
public class ActiveProduct {

    /**
     * Unique identifier for the product.
     */
    @Id
    private String id;

    /**
     * Credit number (unique).
     */
    @Indexed(unique = true)
    private String creditNumber;

    /**
     * Customer ID who owns this credit.
     */
    @Indexed
    private String customerId;

    /**
     * Type of active product (PERSONAL_LOAN, BUSINESS_LOAN, CREDIT_CARD).
     */
    private ActiveProductType productType;

    /**
     * Credit limit.
     */
    private BigDecimal creditLimit;

    /**
     * Available credit.
     */
    private BigDecimal availableCredit;

    /**
     * Used credit.
     */
    private BigDecimal usedCredit;

    /**
     * Interest rate (annual percentage).
     */
    private BigDecimal interestRate;

    /**
     * Currency type.
     */
    private Currency currency;

    /**
     * Day of month for payment due date.
     */
    private Integer paymentDueDate;

    /**
     * Minimum payment amount.
     */
    private BigDecimal minimumPayment;

    /**
     * Outstanding balance to be paid.
     */
    private BigDecimal outstandingBalance;

    /**
     * Overdue amount.
     */
    private BigDecimal overdueAmount;

    /**
     * Indicates if has overdue debt.
     */
    @Indexed
    private Boolean hasOverdueDebt;

    /**
     * Product status.
     */
    private ProductStatus status;

    /**
     * Timestamp when the product was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the product was last updated.
     */
    private LocalDateTime updatedAt;
}
