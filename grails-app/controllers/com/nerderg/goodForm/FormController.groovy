package com.nerderg.goodForm

import com.nerderg.goodForm.form.Form

import com.nerderg.goodForm.form.Question
import net.sf.json.JSONObject
import grails.converters.JSON

/**
 * Controller which manages the display of goodform forms. This class can be subclassed if custom behaviour is required.
 *
 */
class FormController {

    def formDataService

    def rulesEngineService

    def index() {
        render(view: '/form/index', plugin: 'GoodForm')
    }

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
     */
    def createForm(String formName) {
        log.debug "apply: $params"
        try {
            if (!formName) {
                flash.message = message(code: "goodform.formName.supplied")
                return redirect(action: 'index')
            }
            Form form = formDataService.getForm(formName)
            if (!form) {
                flash.message = message(code: "goodform.formName.invalid")
                return redirect(action: 'index')
            }
            Map formData = rulesEngineService.ask(formName, getRuleFacts()) as Map
            FormInstance formInstance = formDataService.createFormInstance(form, formData)
            List ask = formDataService.getSubset(formData.next, form)
            render(view: '/form/formDetails', model: [form: form, asked: [], questions: ask, formData: formData, formInstance: formInstance])
        } catch (RulesEngineException e) {
            flash.message = message(code: "goodform.rules.error", args: [e.message])
            return redirect(action: 'index')
        }
    }

    public Map getRuleFacts() {
        [:]
    }

    def continueForm(Long id) {
        log.debug "continue: $params"
        FormInstance formInstance = formDataService.checkInstance(id)

        if (!formInstance) {
            flash.message = message(code: "goodform.form.invalid", args: [id])
            return redirect(action: 'index')
        }

        if (formInstance.readOnly) {
            flash.message = message(code: "goodform.form.readonly", args: [id])
            redirect(action: 'index')
        }

        Map formData = formInstance.storedFormData()
        Form form = formDataService.getFormQuestions(formInstance.getFormDefinition())

        if (formInstance.isAtEnd()) {
            return redirect(action: 'endForm', id: formInstance.id)
        }

        List<Question> current = formDataService.getSubset(formInstance.storedCurrentQuestion(), form)
        List answered = formDataService.getAnsweredQuestions(formInstance, form)

        render(view: '/form/formDetails', model: [form: form, asked: answered, questions: current, formData: formData, formInstance: formInstance])
    }

    /**
     * Invoked when the 'Submit' button is clicked on a form.
     *
     */
    def next(Long instanceId) {
        log.debug "next: $params"
        FormInstance formInstance = formDataService.checkInstance(instanceId)
        if (!formInstance) {
            flash.message = message(code: "goodform.form.invalid", args: [instanceId])
            return redirect(action: 'index')
        }

        if (formInstance.isAtEnd()) {
            return redirect(action: 'view', id: instanceId)
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
                    return redirect(action: 'endForm', id: formInstance.id)
                }
                List<Question> current = formDataService.getSubset(processedFormData.next, form)
                List answered = formDataService.getAnsweredQuestions(formInstance, form)
                render(view: '/form/formDetails', model: [form: form, asked: answered, questions: current, formData: processedFormData, formInstance: formInstance])
            } catch (RulesEngineException e) {
                //logged in processNext just set the flash message and redirect
                flash.message = message(code: "goodform.rules.error", args: [e.message])
                return redirect(action: 'index')
            }
        } else {
            //error detected, redisplay form
            List answered = formDataService.getAnsweredQuestions(formInstance, form)
            render(view: '/form/formDetails', model: [form: form, asked: answered, questions: asked, formData: mergedFormData, formInstance: formInstance])
        }
    }

    /**
     *  Invoked when a user clicks the 'Back' button on a multi-page form.
     */
    def back(Long id, int qset) {
        log.debug "back: $params"
        FormInstance formInstance = formDataService.checkInstance(id)
        if (!formInstance) {
            flash.message = message(code: "goodform.form.invalid", args: [id])
            return redirect(action: 'index')
        }

        List<List> state = formInstance.storedState()
        List currentQ = state.reverse()[qset]
        formInstance.storeCurrentQuestion(currentQ)
        formInstance.storeState(formDataService.truncateState(state, currentQ))
        formInstance.save(flush: true)
        redirect(action: 'continueForm', id: formInstance.id)
    }

    /**
     * Handles the final submission of a form.
     */
    def endForm(Long id) {
        log.debug "end: $params"
        FormInstance formInstance = formDataService.checkInstance(id)
        if (!formInstance) {
            flash.message = message(code: "goodform.form.invalid", args: [id])
            return redirect(action: 'index')
        }
        Map formData = formInstance.storedFormData()
        try {
            JSONObject processedJSONFormData = rulesEngineService.ask('CheckRequiredDocuments', formData) as JSONObject
            formData = formDataService.cleanUpJSONNullMap(processedJSONFormData)
            formDataService.updateStoredFormInstance(formInstance, formData)
        } catch (RulesEngineException e) {
            log.error e.message
        }
        log.debug "end FormData: ${(formData as JSON).toString(true)}"
        render(view: '/form/endForm', model: [formInstance: formInstance, formData: formData])
    }

    /**
     * Displays a read-only view of a form.
     */
    def view(Long id, String name) {
        log.debug "view: $params"
        FormInstance formInstance = formDataService.checkInstance(id)
        if (!formInstance) {
            flash.message = message(code: "goodform.form.invalid", args: [id])
            return redirect(action: 'index')
        }

        Map formData = formInstance.storedFormData()
        log.debug "view FormData: ${(formData as JSON).toString(true)}"
        if (name) {
            //todo this gets called but it gets a NPE line 38 of PdfRenderingService
            return renderPdf(template: '/form/formView', model: [formInstance: formInstance, formData: formData])
        } else {
            return render(view: '/form/endForm', model: [formInstance: formInstance, formData: formData])
        }
    }
}