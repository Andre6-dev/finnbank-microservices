package com.finnova.auth_service.exception;

public class InvalidCredentialsException extends RuntimeException {

    /**
     * Constructs a new InvalidCredentialsException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
