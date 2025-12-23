package com.iodigital.tedtalks.presentation.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for all REST controllers.
 * Handles exceptions consistently across the application.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation constraint violations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(ConstraintViolationException e) {
        log.warn("Constraint violation: {}", e.getMessage());

        List<String> errors = e.getConstraintViolations().stream()
                .map(violation ->
                        String.format("%s: %s",
                                violation.getPropertyPath(),
                                violation.getMessage()))
                .collect(Collectors.toList());

        return new ErrorResponse("Validation failed", errors);
    }

    /**
     * Handle method argument validation failures
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.warn("Method argument validation failed: {}", e.getMessage());

        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ErrorResponse("Validation failed", errors);
    }

    /**
     * Handle type mismatch in path/request parameters
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch for parameter '{}': {}", e.getName(), e.getValue());

        String expectedType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";
        String error = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                e.getValue(), e.getName(), expectedType);

        return new ErrorResponse("Invalid parameter type", List.of(error));
    }

    /**
     * Handle ResponseStatusException (thrown by controllers)
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException e) {

        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());

        // Log based on severity
        if (status.is5xxServerError()) {
            log.error("Server error: {}", e.getReason(), e);
        } else if (status.is4xxClientError() && status != HttpStatus.NOT_FOUND) {
            log.warn("Client error [{}]: {}", status, e.getReason());
        } else {
            log.debug("Client request issue [{}]: {}", status, e.getReason());
        }

        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(e.getReason(), null));
    }

    /**
     * Handle async operation failures (CompletableFuture)
     */
    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<ErrorResponse> handleCompletionException(CompletionException e) {
        Throwable cause = e.getCause();

        log.error("Async operation failed", cause);

        // If the cause is a known exception, handle it appropriately
        if (cause instanceof ResponseStatusException) {
            return handleResponseStatusException((ResponseStatusException) cause);
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Async operation failed: " + cause.getMessage(), null));
    }

    /**
     * Handle 404 Not Found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NoHandlerFoundException e) {
        log.warn("Endpoint not found: {} {}", e.getHttpMethod(), e.getRequestURL());
        return new ErrorResponse("Endpoint not found", null);
    }

    /**
     * Handle illegal argument exceptions (business logic violations)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), null);
    }

    /**
     * Handle illegal state exceptions (business logic violations)
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIllegalState(IllegalStateException e) {
        log.warn("Illegal state: {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), null);
    }

    /**
     * Catch-all handler for any unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception e) {
        log.error("Unhandled exception", e);

        // Don't expose internal error details in production
        String message = "Internal server error";

        // In development, you might want to see the actual error
        // Uncomment for development (but never in production!)
        // message = e.getMessage();

        return new ErrorResponse(message, null);
    }

    /**
     * Standard error response structure
     */
    public record ErrorResponse(String message, List<String> errors) {}

}