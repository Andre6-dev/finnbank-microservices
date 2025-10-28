package com.finnova.products_service.model.enums;

public enum ProductStatus {

    /**
     * Product is active and operational.
     */
    ACTIVE,

    /**
     * Product is inactive.
     */
    INACTIVE,

    /**
     * Product is blocked.
     */
    BLOCKED,

    /**
     * Product has overdue debt (for active products).
     */
    OVERDUE
}
