package com.finnova.products_service.model.enums;

public enum ActiveProductType {

    /**
     * Personal loan - only one allowed per person.
     */
    PERSONAL_LOAN,

    /**
     * Business loan - multiple allowed per business.
     */
    BUSINESS_LOAN,

    /**
     * Credit card - personal or business.
     */
    CREDIT_CARD
}
