package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class DuplicateSkuException extends CartException {

    public DuplicateSkuException(String sku) {
        super("A product with SKU " + sku + " already exists", HttpStatus.CONFLICT);
    }
}
