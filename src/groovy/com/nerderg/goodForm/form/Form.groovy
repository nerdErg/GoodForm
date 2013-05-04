package com.nerderg.goodForm.form

import com.nerderg.goodForm.FormVersion

/**
 * Form contains the set of form questions retrieved from a form definition.
 *
 * User: pmcneil
 * Date: 11/11/11
 */
class Form {

    def goodFormService
    FormVersion version
    String name
    def parent
    List<Question>  questions = []
    Map<String, Closure> updates = [:]
    Long formDefinitionId

    Question getAt(String index) {
        questions.find { it.ref == index }
    }

    /**
     * Handles the question element in the Form DSL
     * @see Question
     * @param ref
     * @param questionDef
     * @return
     */
    def question(String ref, Closure questionDef) {
        Question q = new Question(ref: ref, parent:  this)
        q.buildQuestion(questionDef)
        questions.add(q)
    }

    /**
     * Handles the update element and adds an updater to the form. Updaters are run on data to update the previous form
     * version data to a new version. This is not the recommended mode of operation. We recommend maintaining a version
     * for the life of the form entry and moving to the new version over time.
     * @param ref
     * @param closure
     * @return
     */
    def update(String ref, Closure closure){
        closure.delegate = new Updater(goodFormService: goodFormService)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        updates.put(ref, closure)
    }

    /**
     * Provides a simple way to print out the entire form structure as the question text and the attributes
     * @return
     */
    def printForm() {
        questions.each { Question q ->
            println "Question $q.ref"
            printFormElements(q.formElement)
        }
    }

    private void printFormElements(e, indent = 0) {
        println indentStr("$e.text? $e.attr", indent)
        ++indent
        e.subElements.each { sub ->
            printFormElements(sub, indent)
        }
    }

    private String indentStr(str, indentCount) {
        if (indentCount > 0) {
            int pad = indentCount * 2 + str.size()
            str.padLeft(pad)
        } else {
            return str
        }
    }
    /**
     * run the update elements against the Map of form data from the previous form version
     * @param map
     */
    void doUpdate(Map map) {
        updates.each { update ->
            Closure closure = update.value
            closure.formData = map
            closure()
        }
    }
}
