package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for all domain exceptions. Carries the HTTP status that
 * GlobalExceptionHandler should map the failure to, so individual
 * subclasses stay free of any web-layer concerns.
 */
public abstract class CartException extends RuntimeException {

    private final HttpStatus status;

    protected CartException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
