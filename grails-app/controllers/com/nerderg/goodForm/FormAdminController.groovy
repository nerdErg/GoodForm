package com.nerderg.goodForm

/**
 * Handles 'administrator' operations for a GoodForm instance, including:
 * <ul>
 * <li>Updating active {@link FormDefinition} instances (eg. to add new form elements)</li>
 * <li>Viewing list of submitted {@link FormInstance}s for a specific {@link FormDefinition}</li>
 * </ul>
 *
 * @author Ross Rowe
 * @author Peter McNeil
 */
class FormAdminController {

    /**
     * Handles retrieving and updating object instances.
     */
    def formDataService

    def index(Long id, Integer version) {
        List forms = FormDefinition.list()
        FormVersion formVersion = getFormVersion(id, forms, version)

        [forms: forms, formVersion: formVersion]
    }

    def edit(Long id, Integer version) {
        List forms = FormDefinition.list()
        FormVersion formVersion = getFormVersion(id, forms, version)
        if (formVersion) {
            [forms: forms, formVersion: formVersion]
        } else {
            redirect(action: 'index')
        }
    }

    private FormVersion getFormVersion(Long id, List<FormDefinition> forms, Integer version) {
        FormDefinition formDefinition
        FormVersion formVersion
        if (id) {
            formDefinition = FormDefinition.get(id)
        } else {
            formDefinition = forms.first()
        }
        if (version) {
            formVersion = formDefinition.formVersions.find { it.formVersionNumber == version }
        } else {
            formVersion = formDefinition.currentVersion()
        }
        return formVersion
    }

    /**
     * Creates a new FormDefinition (incrementing the version number) with the entered form definition text).
     */
    def updateFormDefinition(Long id, String formDefinition) {
        FormVersion from = FormVersion.get(id)
        if (from) {
            FormVersion newFormVersion = formDataService.createNewFormVersion(from.formDefinition, formDefinition)
            if (newFormVersion) {
                flash.message = message(code: "goodform.update.successful")
                redirect(action: 'index', id: newFormVersion.formDefinition.id)
            } else {
                flash.message = message(code: "goodform.update.failed")
                redirect(action: 'edit', id: from.formDefinition.id, params: [version: from.formVersionNumber])
            }
        } else {
            flash.message = message(code: "goodform.update.notfound")
            redirect(action: 'index')
        }
    }

    def create(String formName) {
        if (!formName) {
            flash.message = message(code: "goodform.create.noname")
            redirect(action: 'index')
            return
        }
        if (FormDefinition.findByName(formName)) {
            flash.message = message(code: "goodform.create.exists")
            redirect(action: 'index')
            return
        }
        [forms: FormDefinition.list(), formName: formName]
    }

    def saveNewFormDefinition(String formName, String formDefinition) {
        FormVersion newFormVersion = formDataService.createNewFormVersion(formName, formDefinition)
        if (newFormVersion) {
            flash.message = message(code: "goodform.update.successful")
            redirect(action: 'index', id: newFormVersion.formDefinition.id)
        } else {
            flash.message = message(code: "goodform.update.failed")
            redirect(action: 'index')
        }
    }
}
