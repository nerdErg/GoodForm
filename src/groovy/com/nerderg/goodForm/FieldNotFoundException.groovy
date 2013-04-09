package com.nerderg.goodForm

/**
 * Exception that is thrown when invalid or missing fields are detected.
 *
 * @author Peter McNeil
 */
class FieldNotFoundException extends GoodFormException {
    FieldNotFoundException() {
    }

    FieldNotFoundException(String msg) {
        super(msg)
    }

    FieldNotFoundException(String msg, Throwable throwable) {
        super(msg, throwable)
    }

    FieldNotFoundException(Throwable throwable) {
        super(throwable)
    }
}
