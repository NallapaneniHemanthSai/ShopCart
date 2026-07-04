package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class InsufficientLoyaltyPointsException extends CartException {

    public InsufficientLoyaltyPointsException(int requested, int available) {
        super("Cannot redeem " + requested + " points: only " + available + " available", HttpStatus.BAD_REQUEST);
    }
}
