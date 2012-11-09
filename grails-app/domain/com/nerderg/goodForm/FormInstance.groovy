package com.nerderg.goodForm

import grails.converters.JSON

/**
 * Represents a form submission for a user.
 *
 * Copied from GrantApplication
 */
class FormInstance {

    Date started
    String givenNames
    String lastName
    String formData
    String state
    String userId
    String currentQuestion
    Long formVersion
    Date lastUpdated

    List storedState() {
        JSON.parse(state) as List
    }

    Map storedFormData() {
        JSON.parse(formData) as Map
    }

    def storeState(List state) {
        this.state = (state as JSON) as String
    }

    def storeFormData(Map formData) {
        this.formData = (formData as JSON) as String
    }

    def storedCurrrentQuestion() {
        JSON.parse(currentQuestion) as List
    }

    def storeCurrentQuestion(List questions) {
        this.currentQuestion = (questions as JSON) as String
    }

    static mapping = {
        formData column: "form_data", sqlType: "text"
    }

    static constraints = {
        givenNames(maxSize: 200)
        lastName(maxSize: 200)
        state(maxSize: 1000)
        userId(maxSize: 40)
        currentQuestion(maxSize: 200)
        lastUpdated(nullable: true)
    }
}
