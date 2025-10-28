package com.finnova.products_service.model.enums;

public enum PassiveProductType {
    /**
     * Savings account - free of maintenance commission, with monthly transaction limit.
     */
    SAVINGS,

    /**
     * Checking account - has maintenance commission, unlimited monthly transactions.
     */
    CHECKING,

    /**
     * Fixed-term account - free of maintenance commission,
     * only allows one withdrawal/deposit on specific day of month.
     */
    FIXED_TERM
}
