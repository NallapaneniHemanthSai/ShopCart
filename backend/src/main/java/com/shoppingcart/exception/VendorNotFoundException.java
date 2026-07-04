package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class VendorNotFoundException extends CartException {

    public VendorNotFoundException(Long id) {
        super("Vendor not found: " + id, HttpStatus.NOT_FOUND);
    }
}
