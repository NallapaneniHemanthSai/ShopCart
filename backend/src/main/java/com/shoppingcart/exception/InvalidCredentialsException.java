package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends CartException {

    public InvalidCredentialsException() {
        super("Invalid email or password", HttpStatus.UNAUTHORIZED);
    }
}
