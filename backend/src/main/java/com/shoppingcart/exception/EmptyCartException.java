package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class EmptyCartException extends CartException {

    public EmptyCartException() {
        super("Cart is empty; add items before checking out", HttpStatus.BAD_REQUEST);
    }
}
