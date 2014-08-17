package com.nerderg.goodForm.form

/**
 * Represents a element within a form. A form consists of a list of questions, with each question containing a form
 * element. Form elements contain sub elements which are FormElements. Each form element has a link to it's parent
 * which may be a form element or a question.
 *
 * @author pmcneil
 * @since 11/11/11
 */
class FormElement {

    String text = "No Text"
    Map attr = [:]
    List<FormElement> subElements = []
    def parent
    String qref

    /**
     * Construct a form element with the question reference
     * @param qref
     */
    FormElement(String qref) {
        this.qref = qref
    }
    /**
     * Define the element from a form element definition closure
     * @param text the question text
     * @param attr attributes associated with the element
     * @param parent
     * @param formElementDef
     */
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

    /**
     * Used to construct sub elements from the Form Element Definition.
     * @param name
     * @param args
     * @return
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
        FormElement e = new FormElement(qref)
        e.form(name, attr, this, formElementDef)
        subElements.add(e)
    }

    /**
     * Used to construct sub elements from a simple property name
     * @param name
     * @return
     */
    def propertyMissing(String name) {
        FormElement e = new FormElement(qref)
        e.text = name
        e.parent = this
        subElements.add(e)
    }

}
