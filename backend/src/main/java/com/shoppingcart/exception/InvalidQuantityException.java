package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class InvalidQuantityException extends CartException {

    public InvalidQuantityException(int quantity) {
        super("Quantity must be a positive integer, received: " + quantity, HttpStatus.BAD_REQUEST);
    }
}
