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

    def pdfRenderingService

    /**
     * Creates a new form instance.
     *
     * Pre conditions:
     * <ul>
     * <li>FormDefinition must be defined in the database (typically in Bootstrap).</li>
     * <li>Form name must be supplied as an input parameter (eg. http://localhost:8080/your-app/form/createForm?form=jobForm Instance)</li>
     * </ul>
     * TODO retrieve form name from input parameters and redirect to error page if not supplied
     * TODO redirect to error page when FormDefinition not supplied
     *
     */
    def createForm = {
        log.debug "apply: $params"
        try {
            String formName = params.formName
            if (!formName) {
                flash.message = message(code: "goodform.formName.supplied")
                return redirect(action: 'index')
            }
            Form form = formDataService.getForm(formName)
            Map formData = rulesEngineService.ask(formName, [:])
            FormInstance formInstance = formDataService.createFormInstance(form, formData)
            List ask = formDataService.getSubset(formData.next, form)
            render(view: '/form/formDetails', model: [form: form, asked: [], questions: ask, formData: formData, formInstance: formInstance])
        } catch (RulesEngineException e) {
            flash.message = message(code: "goodform.rules.error", args: [e.message])
            return redirect(action: 'index')
        }
    }

    /**
     * Invoked when the 'Submit' button is clicked on a form.
     *
     */
    def next = {
        log.debug "next: $params"
        FormInstance formInstance = formDataService.checkInstance(params.instanceId as Long)
        if (!formInstance) {
            flash.message = message(code: "goodform.form.invalid", args: [params.instanceId])
            return redirect(action: 'apply')
        }

        Map currentFormData = formDataService.cleanUpStateParams(params)
        Form form = formDataService.getFormQuestions(formInstance.getFormDefinition())
        List asked = formDataService.getSubset(formInstance.storedCurrentQuestion(), form)

        //todo re look at this merged Data as we should merge after validate? Also multiple calls to storedFormData
        Map mergedFormData = rulesEngineService.cleanUpJSONNullMap(formInstance.storedFormData()) << currentFormData
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
                log.debug "next processedFormData: ${processedFormData.toString(2)}"
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
                return redirect(action: 'apply')
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
    def back = {
        log.debug "back: $params"
        FormInstance formInstance = formDataService.checkInstance(params.id as Long)
        if (!formInstance) {
            flash.message = message(code: "goodform.form.invalid", args: [params.id])
            return redirect(action: 'apply')
        }

        List state = formInstance.storedState()
        List currentQ = state.reverse()[params.qset as int]
        formInstance.storeCurrentQuestion(currentQ)
        formInstance.storeState(formDataService.truncateState(state, currentQ))
        formInstance.save(flush: true)
        redirect(action: 'continueApp', id: formInstance.id)
    }

    /**
     * Handles the final submission of a form.
     */
    def endForm = {
        log.debug "end: $params"
        FormInstance formInstance = formDataService.checkInstance(params.id as Long)
        if (!formInstance) {
            flash.message = message(code: "goodform.form.invalid", args: [params.id])
            return redirect(action: 'apply')
        }
        Map formData = formInstance.storedFormData()
        JSONObject processedJSONFormData = rulesEngineService.ask('CheckRequiredDocuments', formData)
        formData = rulesEngineService.cleanUpJSONNullMap(processedJSONFormData)
        formDataService.updateStoredFormInstance(formInstance, formData)
        log.debug "end FormData: ${(formData as JSON).toString(true)}"
        render(view: '/form/endForm', model: [formInstance: formInstance, formData: formData])
    }

    /**
     * Displays a read-only view of a form.
     */
    def view = {
        log.debug "view: $params"
        FormInstance formInstance = formDataService.checkInstance(params.id as Long)
        if (!formInstance) {
            flash.message = message(code: "goodform.form.invalid", args: [params.id])
            return redirect(action: 'apply')
        }

        Map formData = formInstance.storedFormData()
        log.debug "view FormData: ${(formData as JSON).toString(true)}"
        if (params.name) {
            return renderPdf([template: '/form/formView', model: [formInstance: formInstance, formData: formData]])
        } else {
            return [formInstance: formInstance, formData: formData]
        }
    }
}