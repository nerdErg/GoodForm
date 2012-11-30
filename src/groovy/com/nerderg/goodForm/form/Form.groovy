package com.nerderg.goodForm.form

import com.nerderg.goodForm.GoodFormService

/**
 * User: pmcneil
 * Date: 11/11/11
 * 
 */
class Form {

    def goodFormService
    Long version
    String name
    def parent = null
    List<Question>  questions = []
    Map<String, Closure> updates = [:]
    Long formDefinitionId

    Question getAt(String index) {
        Question q = questions.find{
            it.ref == index
        }
        return q
    }

    def question(String ref, Closure questionDef) {
        Question q = new Question(ref: ref, parent:  this)
        q.buildQuestion(questionDef)
        questions.add(q)
    }

    def update(String ref, Closure closure){
        closure.delegate = new Updater(goodFormService: goodFormService)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        updates.put(ref, closure)
    }

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

    void doUpdate(Map map) {
        updates.each { update ->
            Closure closure = update.value
            closure.formData = map
            closure()
        }
    }
}
