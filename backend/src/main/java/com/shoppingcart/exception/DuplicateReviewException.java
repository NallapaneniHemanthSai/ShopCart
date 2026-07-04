package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class DuplicateReviewException extends CartException {

    public DuplicateReviewException(String sku) {
        super("You have already reviewed product: " + sku, HttpStatus.CONFLICT);
    }
}
