package com.finnova.products_service.model.entity;

import com.finnova.products_service.model.enums.Currency;
import com.finnova.products_service.model.enums.PassiveProductType;
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
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "passive_products")
public class PassiveProduct {

    /**
     * Unique identifier for the product.
     */
    @Id
    private String id;

    /**
     * Account number (unique).
     */
    @Indexed(unique = true)
    private String accountNumber;

    /**
     * Customer ID who owns this account.
     */
    @Indexed
    private String customerId;

    /**
     * Type of passive product (SAVINGS, CHECKING, FIXED_TERM).
     */
    private PassiveProductType productType;

    /**
     * Current balance.
     */
    private BigDecimal balance;

    /**
     * Currency type.
     */
    private Currency currency;

    /**
     * Minimum amount required to open the account.
     */
    private BigDecimal openingAmount;

    /**
     * Monthly maintenance fee.
     */
    private BigDecimal maintenanceFee;

    /**
     * Maximum number of transactions without commission per month.
     */
    private Integer maxTransactionsWithoutFee;

    /**
     * Current month transaction count.
     */
    private Integer currentMonthTransactions;

    /**
     * Fee charged per extra transaction.
     */
    private BigDecimal feePerExtraTransaction;

    /**
     * Specific day of month for movements (FIXED_TERM only).
     */
    private Integer movementDay;

    /**
     * Minimum daily average balance (VIP accounts).
     */
    private BigDecimal minimumDailyAverage;

    /**
     * Product status.
     */
    private ProductStatus status;

    /**
     * List of account holders (for business accounts).
     */
    private List<String> holders;

    /**
     * List of authorized signers (for business accounts).
     */
    private List<String> authorizedSigners;

    /**
     * Timestamp when the product was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the product was last updated.
     */
    private LocalDateTime updatedAt;
}
