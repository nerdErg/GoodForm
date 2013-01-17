package com.nerderg.goodForm

/**
 * @author Ross Rowe
 */
class InvalidFormDefinitionException extends GoodFormException {
    InvalidFormDefinitionException() {
    }

    InvalidFormDefinitionException(String msg) {
        super(msg)
    }

    InvalidFormDefinitionException(String msg, Throwable throwable) {
        super(msg, throwable)
    }

    InvalidFormDefinitionException(Throwable throwable) {
        super(throwable)
    }
}
