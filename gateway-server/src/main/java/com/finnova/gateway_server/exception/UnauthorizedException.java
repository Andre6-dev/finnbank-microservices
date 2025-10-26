package com.finnova.gateway_server.exception;

/**
 * Unauthorized Exception.
 * Thrown when authentication or authorization fails.
 *
 * @author Andre Gallegos
 * @version 1.0.0
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedException with the specified detail message.
     *
     * @param message the detail message
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Constructs a new UnauthorizedException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
