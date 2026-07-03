package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class InvalidCouponException extends CartException {

    public InvalidCouponException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
