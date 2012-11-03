package com.nerderg.goodForm

/**
 * User: pmcneil
 * Date: 6/08/12
 * 
 */
class FieldNotFoundException extends Exception {
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
