package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class CartItemNotFoundException extends CartException {

    public CartItemNotFoundException(String sku) {
        super("No cart item found for SKU: " + sku, HttpStatus.NOT_FOUND);
    }
}
