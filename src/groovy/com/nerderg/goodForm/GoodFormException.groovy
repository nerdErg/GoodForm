package com.nerderg.goodForm

/**
 * Common superclass for all GoodForm exceptions.
 *
 * @author Ross Rowe
 */
class GoodFormException extends Exception {
    GoodFormException() {
    }

    GoodFormException(Throwable throwable) {
        super(throwable)
    }

    GoodFormException(String s) {
        super(s)
    }
}
