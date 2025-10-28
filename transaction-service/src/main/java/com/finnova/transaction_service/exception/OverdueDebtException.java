package com.finnova.transaction_service.exception;

public class OverdueDebtException extends RuntimeException {
    public OverdueDebtException(String message) {
        super(message);
    }
}
