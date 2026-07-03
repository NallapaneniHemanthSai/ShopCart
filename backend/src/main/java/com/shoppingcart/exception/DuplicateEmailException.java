package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends CartException {

    public DuplicateEmailException(String email) {
        super("An account already exists with email: " + email, HttpStatus.CONFLICT);
    }
}
