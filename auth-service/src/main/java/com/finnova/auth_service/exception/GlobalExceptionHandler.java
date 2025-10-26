package com.finnova.auth_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles UserAlreadyExistsException.
     *
     * @param ex the exception
     * @return Mono of ResponseEntity with error details
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex) {
        log.error("User already exists: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage())));
    }

    /**
     * Handles InvalidCredentialsException.
     *
     * @param ex the exception
     * @return Mono of ResponseEntity with error details
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleInvalidCredentialsException(
            InvalidCredentialsException ex) {
        log.error("Invalid credentials: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage())));
    }

    /**
     * Handles UserNotFoundException.
     *
     * @param ex the exception
     * @return Mono of ResponseEntity with error details
     */
    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleUserNotFoundException(
            UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage())));
    }

    /**
     * Handles validation exceptions.
     *
     * @param ex the exception
     * @return Mono of ResponseEntity with validation errors
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationException(
            WebExchangeBindException ex) {
        log.error("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Validation Failed");
        errorResponse.put("errors", errors);

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse));
    }

    /**
     * Handles all other exceptions.
     *
     * @param ex the exception
     * @return Mono of ResponseEntity with error details
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGeneralException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred")));
    }

    /**
     * Builds an error response map.
     *
     * @param status the HTTP status
     * @param message the error message
     * @return the error response map
     */
    private Map<String, Object> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        return errorResponse;
    }
}
