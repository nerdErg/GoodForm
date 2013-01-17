package com.nerderg.goodForm

import grails.converters.JSON

/**
 * Represents a form submission for a user.
 */
class FormInstance {

    Date started
    String instanceDescription // eg first and last name
    String formData
    String state
    String userId
    String currentQuestion
    Long formVersion
    Long formDefinitionId
    Date lastUpdated
    Boolean readOnly

    List<List> storedState() {
        JSON.parse(state) as List
    }

    Map storedFormData() {
        JSON.parse(formData) as Map
    }

    def storeState(List<List> state) {
        this.state = (state as JSON) as String
    }

    def storeFormData(Map formData) {
        this.formData = (formData as JSON) as String
    }

    def storedCurrentQuestion() {
        JSON.parse(currentQuestion) as List
    }

    def storeCurrentQuestion(List questions) {
        this.currentQuestion = (questions as JSON) as String
    }

    boolean isAtEnd() {
        List current = storedCurrentQuestion()
        return current.size() == 1 && current.first() == 'End'
    }

    static mapping = {
        formData column: "form_data", sqlType: "text"
    }

    static constraints = {
        instanceDescription(maxSize: 200)
        state(maxSize: 1000)
        userId(maxSize: 40)
        currentQuestion(maxSize: 200)
        lastUpdated(nullable: true)
    }

    FormDefinition getFormDefinition() {
        //find FormDefinition for id
        FormDefinition.get(formDefinitionId)
    }
}
