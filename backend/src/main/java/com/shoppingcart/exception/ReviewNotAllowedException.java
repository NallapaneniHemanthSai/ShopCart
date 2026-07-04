package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class ReviewNotAllowedException extends CartException {

    public ReviewNotAllowedException() {
        super("You can only review products you have purchased", HttpStatus.FORBIDDEN);
    }
}
