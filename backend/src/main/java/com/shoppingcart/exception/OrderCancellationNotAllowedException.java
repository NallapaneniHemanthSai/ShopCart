package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class OrderCancellationNotAllowedException extends CartException {

    public OrderCancellationNotAllowedException(String invoiceNumber) {
        super("Order " + invoiceNumber + " has already been cancelled", HttpStatus.CONFLICT);
    }
}
