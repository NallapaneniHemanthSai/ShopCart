package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends CartException {

    public ProductNotFoundException(String sku) {
        super("Product not found for SKU: " + sku, HttpStatus.NOT_FOUND);
    }
}
