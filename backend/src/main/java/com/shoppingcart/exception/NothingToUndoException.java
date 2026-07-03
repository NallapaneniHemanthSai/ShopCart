package com.shoppingcart.exception;

import org.springframework.http.HttpStatus;

public class NothingToUndoException extends CartException {

    public NothingToUndoException() {
        super("There is no cart operation to undo", HttpStatus.BAD_REQUEST);
    }
}
