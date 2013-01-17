package com.nerderg.goodForm

import com.nerderg.goodForm.form.FormElement

/**
 * User: pmcneil
 * Date: 6/08/12
 * 
 */
class FieldNotMappedException extends GoodFormException {
    FieldNotMappedException() {
    }

    FieldNotMappedException(FormElement element) {
        super("element $element.parent.attr.name - $element.text is not mapped".toString())
    }

    FieldNotMappedException(String msg) {
        super(msg)
    }

    FieldNotMappedException(String msg, Throwable throwable) {
        super(msg, throwable)
    }

    FieldNotMappedException(Throwable throwable) {
        super(throwable)
    }
}
