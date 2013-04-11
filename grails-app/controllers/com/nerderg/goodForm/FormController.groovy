package com.nerderg.goodForm

import grails.converters.JSON
import com.nerderg.goodForm.form.Form
import com.nerderg.goodForm.form.Question
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Controller which manages the display of <a href="http://nerderg.com/Good+Form">GoodForm</a> forms.
 *
 * This class can be subclassed if custom behaviour is required.
 *
 * @author Peter McNeil
 */
class FormController {

    def formDataService

    def rulesEngineService

    /**
     * Default view. By default this allows you to create a new form from the list of form instances available.
     * @return
     */
    def index() {
        render(view: '/form/index', plugin: 'GoodForm')
    }

    /**
     * A place holder action for the final submission of a completed form. You should override this action to handle the
     * completed form data.
     *
     * @param id
     * @return
     */
    def submit(Long id) {
        render(view: '/form/submit', plugin: 'GoodForm')
    }

    /**
     * Creates a new form instance.
     *
     * Pre conditions:
     * <ul>
     * <li>FormDefinition must be defined in the database (typically in Bootstrap).</li>
     * <li>Form name must be supplied as an input parameter (eg. http://localhost:8080/your-app/form/createForm?form=jobForm Instance)</li>
     * </ul>
     *
     * @param formName the name of the form definition.  Must be a reference to an existing form definition, or the request will be redirected back to the index
     * with an error
     *
     */
    def createForm(String formName) {
        log.debug "apply: $params"
        try {
            if (!formName) {
                flash.message = message(code: "goodform.formName.supplied")
                redirect(action: 'index')
                return
            }
            Form form = formDataService.getForm(formName)
            if (!form) {
                flash.message = message(code: "goodform.formName.invalid")
                redirect(action: 'index')
                return
            }
            Map formData = rulesEngineService.ask(formName, getRuleFacts()) as Map
            FormInstance formInstance = formDataService.createFormInstance(form, formData)
            List ask = formDataService.getSubset(formData.next, form)
            render(view: '/form/formDetails', model: [form: form, asked: [], questions: ask, formData: formData, formInstance: formInstance])
        } catch (RulesEngineException e) {
            flash.message = message(code: "goodform.rules.error", args: [e.message])
            redirect(action: 'index')
        }
    }

    /**
     * The default behaviour is to return an empty map, but subclasses can override this method to provide a custom
     * map of applicable rule facts.
     * @return map of rule faces
     */
    protected Map getRuleFacts() {
        [:]
    }

    /**
     * Continue editing a form from where it was left. This is called from the 'Back' action after setting the next
     * question attribute in the form data.
     * @param id
     * @return
     */
    def continueForm(Long id) {
        log.debug "continue: $params"
        FormInstance formInstance = formDataService.getFormInstance(id)

        if (!formInstance) {
            flash.message = message(code: "goodform.form.invalid", args: [id])
            redirect(action: 'index')
            return
        }

        if (formInstance.readOnly) {
            flash.message = message(code: "goodform.form.readonly", args: [id])
            redirect(action: 'index')
            return
        }

        Map formData = formInstance.storedFormData()
        Form form = formDataService.getFormQuestions(formInstance.getFormDefinition())

        if (formInstance.isAtEnd()) {
            redirect(action: 'endForm', id: formInstance.id)
            return
        }

        List<Question> current = formDataService.getSubset(formInstance.storedCurrentQuestion(), form)
        List answered = formDataService.getAnsweredQuestions(formInstance, form)

        render(view: '/form/formDetails', model: [form: form, asked: answered, questions: current, formData: formData, formInstance: formInstance])
    }

    /**
     * Invoked when the 'Submit' button is clicked on a form. This action processes the form data and then shows the
     * next set of questions based on the response from the rules engine.
     *
     * @param instanceId the form instance id.
     */
    def next(Long instanceId) {
        log.debug "next: $params"
        FormInstance formInstance = formDataService.getFormInstance(instanceId)
        if (!formInstance) {
            flash.message = message(code: "goodform.form.invalid", args: [instanceId])
            redirect(action: 'index')
            return
        }

        if (formInstance.isAtEnd()) {
            redirect(action: 'view', id: instanceId)
            return
        }

        Map currentFormData = formDataService.cleanUpStateParams(params)
        Form form = formDataService.getFormQuestions(formInstance.getFormDefinition())
        List asked = formDataService.getSubset(formInstance.storedCurrentQuestion(), form)

        //todo re look at this merged Data as we should merge after validate? Also multiple calls to storedFormData
        Map mergedFormData = formDataService.cleanUpJSONNullMap(formInstance.storedFormData()) << currentFormData
        mergedFormData.formVersion = formInstance.formVersion

        formInstance.storedCurrentQuestion().each { ref ->
            if (mergedFormData[ref]?.recheck) {
                (mergedFormData[ref] as Map).remove('recheck')
            }
        }

        boolean error = false
        asked.each { Question question ->
            //note the or error here makes sure error isn't reset whilst checking all form elements (so don't move it in front :-)
            error = formDataService.validateAndProcessFields(question.formElement, mergedFormData, formInstance) || error
        }
        formInstance.storeFormData(mergedFormData)

        if (!error) {
            try {
                Map processedFormData = formDataService.processNext(formInstance, mergedFormData)
                log.debug "next processedFormData: ${processedFormData.toString()}"
                //rules engine returns "End" as the next question at the end
                if (processedFormData.next.size() == 1 && processedFormData.next[0] == 'End') {
                    redirect(action: 'endForm', id: formInstance.id)
                    return
                }
                List<Question> current = formDataService.getSubset(processedFormData.next, form)
                List answered = formDataService.getAnsweredQuestions(formInstance, form)
                render(view: '/form/formDetails', model: [form: form, asked: answered, questions: current, formData: processedFormData, formInstance: formInstance])
            } catch (RulesEngineException e) {
                //logged in processNext just set the flash message and redirect
                flash.message = message(code: "goodform.rules.error", args: [e.message])
                redirect(action: 'index')
            }
        } else {
            //error detected, redisplay form
            List answered = formDataService.getAnsweredQuestions(formInstance, form)
            render(view: '/form/formDetails', model: [form: form, asked: answered, questions: asked, formData: mergedFormData, formInstance: formInstance])
        }
    }

    /**
     *  Invoked when a user clicks the 'Back' button on a multi-page form. The back button is the form panel you wish to
     *  edit. The javascript for the panel supplies the question set to go back to as an index into the 'state' variable.
     *
     * @param id the form instance id
     * @param questionSetIndex the index of the question set in the state array
     */
    def back(Long id, int qset) {
        log.debug "back: $params"
        FormInstance formInstance = formDataService.getFormInstance(id)
        if (!formInstance) {
            flash.message = message(code: "goodform.form.invalid", args: [id])
            redirect(action: 'index')
            return
        }

        List<List> state = formInstance.storedState()
        List currentQ = state.reverse()[qset]
        formInstance.storeCurrentQuestion(currentQ)
        formInstance.storeState(formDataService.truncateState(state, currentQ))
        formInstance.save(flush: true)
        redirect(action: 'continueForm', id: formInstance.id)
    }

    /**
     * Handles the end of a form. If the next questions returned by the rules engine is 'End' it redirects to here to
     * display the form in a text readable format. Here we ask the rules engine to check all required fields and documents
     * have been supplied. Documents and optional fields may have been skipped, but are required based on certain
     * responses to questions.
     *
     * @param id the form instance id
     */
    def endForm(Long id) {
        log.debug "end: $params"
        FormInstance formInstance = formDataService.getFormInstance(id)
        if (!formInstance) {
            flash.message = message(code: "goodform.form.invalid", args: [id])
            redirect(action: 'index')
            return
        }
        Map formData = formInstance.storedFormData()
        try {
            JSONObject processedJSONFormData = rulesEngineService.ask("${formInstance.formDefinition.name}CheckRequiredDocuments", formData) as JSONObject
            formData = formDataService.cleanUpJSONNullMap(processedJSONFormData)
            formDataService.updateStoredFormInstance(formInstance, formData)
        } catch (RulesEngineException e) {
            flash.message = e.message
            log.error e.message
        }
        log.debug "end FormData: ${(formData as JSON).toString(true)}"
        render(view: '/form/endForm', model: [formInstance: formInstance, formData: formData])
    }

    /**
     * Displays a text view of a form. This displays the form in a text readable format. If the form is editable, clicking
     * on a form pane will take you back to edit that panel.
     *
     * If a name parameter is supplied then a PDF rendered version of this view will be returned to the browser. The name f
     * orms part of the url and so the pdf version will be given the name by default.
     *
     * @param id form instance id
     * @param name the name given to the PDF rendered file.
     */
    def view(Long id, String name) {
        log.debug "view: $params"
        FormInstance formInstance = formDataService.getFormInstance(id)
        if (!formInstance) {
            flash.message = message(code: "goodform.form.invalid", args: [id])
            redirect(action: 'index')
            return
        }

        Map formData = formInstance.storedFormData()
        log.debug "view FormData: ${(formData as JSON).toString(true)}"
        if (name) {
            //todo this gets called but it gets a NPE line 38 of PdfRenderingService
            renderPdf(template: '/form/formView', model: [formInstance: formInstance, formData: formData])
        } else {
            render(view: '/form/endForm', model: [formInstance: formInstance, formData: formData])
        }
    }
}
