package com.finnova.customer_service.model.enums;

public enum ProfileType {
    /**
     * Standard profile - regular customer.
     */
    STANDARD,

    /**
     * VIP profile - personal customer with special privileges.
     * Requires credit card to open VIP accounts.
     */
    VIP,

    /**
     * PYME profile - small and medium business.
     * Requires credit card to open PYME accounts.
     */
    PYME
}
