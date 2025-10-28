package com.finnova.products_service.exception;

public class InvalidProductOperationException extends RuntimeException {
    public InvalidProductOperationException(String message) {
        super(message);
    }
}
