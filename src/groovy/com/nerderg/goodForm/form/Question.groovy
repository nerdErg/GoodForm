package com.nerderg.goodForm.form

/**
 * User: pmcneil
 * Date: 11/11/11
 *
 */
class Question {
    String ref
    Form parent
    Map attr = [:]
    FormElement formElement

    def buildQuestion(Closure formElementDef) {
        attr.name = ref
        formElementDef.delegate = this
        formElementDef()
    }

    def methodMissing(String name, args) {
        Map attr = [:]
        Closure formElementDef = null
        args.each { arg ->
            if (arg instanceof Map) {
                attr = arg
            }
            if (arg instanceof Closure) {
                formElementDef = arg
            }
        }
        formElement = new FormElement(ref)
        formElement.form(name, attr, this, formElementDef)
    }
}
