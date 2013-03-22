package com.nerderg.goodForm.form

/**
 * The Form Question defines a Question as a Form Element. The question has a reference string to identify it.
 *
 * User: pmcneil
 * Date: 11/11/11
 *
 */
class Question {
    String ref
    Form parent
    Map attr = [:]
    FormElement formElement

    /**
     * Construct a question from a Form Element definition closure
     * @param formElementDef
     * @return
     */
    def buildQuestion(Closure formElementDef) {
        attr.name = ref
        formElementDef.delegate = this
        formElementDef()
    }

    /**
     * Create the base form element from the contents of the Form Element definition closure.
     * @param name
     * @param args
     */
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
