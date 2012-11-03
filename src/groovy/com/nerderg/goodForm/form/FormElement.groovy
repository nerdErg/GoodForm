package com.nerderg.goodForm.form

/**
 * User: pmcneil
 * Date: 11/11/11
 *
 */
class FormElement {

    String text = "No Text"
    Map attr = [:]
    List<FormElement> subElements = []
    def parent
    String qref

    FormElement(String qref) {
        this.qref = qref
    }

    void form(String text, Map attr, parent, Closure formElementDef) {
        this.text = text
        this.attr = attr
        this.parent = parent
        if (formElementDef) {
            formElementDef.delegate = this
            formElementDef.resolveStrategy = Closure.DELEGATE_ONLY
            formElementDef()
        }
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
        FormElement e = new FormElement(qref)
        e.form(name, attr, this, formElementDef)
        subElements.add(e)
    }

    def propertyMissing(String name) {
        FormElement e = new FormElement(qref)
        e.text = name
        e.parent = this
        subElements.add(e)
    }

}
