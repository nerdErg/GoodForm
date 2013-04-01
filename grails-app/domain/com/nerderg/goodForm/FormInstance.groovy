package com.nerderg.goodForm

import grails.converters.JSON

/**
 * Represents a form submission for a user.
 *
 * @author Peter McNeil
 */
class FormInstance {

    Date started //the date this form was created
    String instanceDescription // eg first and last name
    String formData //JSON representation of the form data map
    String state //JSON list of the question sets that have been answered in the form
    String userId //an identifier of the user that filled in this form
    String currentQuestion //JSON list of question references that are to be displayed
    Long formVersion //the form definitions version number so we can map the definition to the data
    Long formDefinitionId //The id of the form definition domain object related to this form
    Date lastUpdated
    Boolean readOnly //if this is true the form is read only and can't be edited.

    /**
     * Retrieve the form state as a List. This parses the JSON list and returns a list of lists.
     * @return a list of question sets
     */
    List<List> storedState() {
        JSON.parse(state) as List
    }

    /**
     * Retrieve the form data as a Map. This parses the JSON form data map and returns a map
     * @return form data as a Map
     */
    Map storedFormData() {
        JSON.parse(formData) as Map
    }

    /**
     * Store the state, a list of question sets, as JSON
     * @param state
     */
    void storeState(List<List> state) {
        this.state = (state as JSON) as String
    }

    /**
     * Store the form data map as JSON
     * @param formData
     */
    void storeFormData(Map formData) {
        this.formData = (formData as JSON) as String
    }

    /**
     * Retrieve the current question set as a list of String. This parses the JSON current question list.
     * @return list of question references
     */
    List<String> storedCurrentQuestion() {
        JSON.parse(currentQuestion) as List
    }

    /**
     * Store the list of question references as JSON
     * @param questions
     */
    void storeCurrentQuestion(List questions) {
        this.currentQuestion = (questions as JSON) as String
    }

    /**
     * Convenience method to check if this form instance is currently at the end of questions to be asked.
     * @return true if at the end, i.e. the current question is 'End'
     */
    boolean isAtEnd() {
        List current = storedCurrentQuestion()
        return current.size() == 1 && current.first() == 'End'
    }

    /**
     * Convenience method to get the form definition associated with this form instance
     * @return
     */
    FormDefinition getFormDefinition() {
        //find FormDefinition for id
        FormDefinition.get(formDefinitionId)
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

}
