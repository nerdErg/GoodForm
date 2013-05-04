package com.nerderg.goodForm

/**
 * Handles 'administrator' operations for a GoodForm instance, including:
 * <ul>
 * <li>Updating active {@link FormDefinition} instances (eg. to add new form elements)</li>
 * <li>Viewing list of submitted {@link FormInstance}s for a specific {@link FormDefinition}</li>
 * </ul>
 *
 * @author Ross Rowe
 */
class FormAdminController {

    /**
     * Handles retrieving and updating object instances.
     */
    def formDataService

    def index() {
        redirect(action: 'listFormDefinitions')
    }

    /**
     * Lists all the active {@link FormDefinition} records.
     */
    def listFormDefinitions() {
       [ formDefinitions: FormDefinition.list()]
    }

    /**
     * Renders the form definition.
     */
    def showFormDefinition(Long id) {
        [formDefinition:  FormVersion.get(id)]
    }

    /**
     * Creates a new FormDefinition (incrementing the version number) with the entered form definition text).
     */
    def updateFormDefinition(Long id, String formDefinition) {
        FormVersion newFormVersion = formDataService.createNewFormVersion(id, formDefinition)
        if (newFormVersion) {
            flash.message = message(code: "goodform.update.successful")
            redirect action: 'showFormDefinition', params: [id: newFormVersion.id]
        } else {
            flash.message = message(code: "goodform.update.failed")
            redirect action: 'showFormDefinition', params: [id: id]
        }
    }

    /**
     * Lists the submitted forms for a specific {@link FormDefinition}.
     */
    def listForms(Long id) {
        [ forms: formDataService.getForms(id), formDefinition: FormDefinition.get(id)]
    }

    /**
     * Renders the form details.
     */
    def viewFormData(Long id) {
        [form : formDataService.getForm(id)]
    }
}
