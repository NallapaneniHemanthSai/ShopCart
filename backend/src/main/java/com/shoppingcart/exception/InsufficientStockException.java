package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends CartException {

    public InsufficientStockException(String sku, int requested, int available) {
        super("Insufficient stock for SKU " + sku + ": requested " + requested + ", available " + available,
                HttpStatus.CONFLICT);
    }
}
