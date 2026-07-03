package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends CartException {

    public OrderNotFoundException(String reference) {
        super("Order not found: " + reference, HttpStatus.NOT_FOUND);
    }
}
