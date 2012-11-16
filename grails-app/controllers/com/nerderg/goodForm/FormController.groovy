package com.nerderg.goodForm

import com.nerderg.goodForm.form.Form

import com.nerderg.goodForm.form.Question
import net.sf.json.JSONObject
import grails.converters.JSON

/**
 * Controller which manages the display of goodform forms.
 *
 *
 */
class FormController {

    def formDataService

    def rulesEngineService

    def applicationService

    def index = {}

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
            Map formData = rulesEngineService.ask(formName, [loginType: whoIs()])
            FormInstance instance = formDataService.createFormInstance(form, formData)
            List ask = formDataService.getSubset(formData.next, form)
            render(view: '/form/formDetails', model: [form: form, asked: [], questions: ask, formData: formData, instance: instance])
        } catch (RulesEngineException e) {
            flash.message = message(code:  "goodform.rules.error", args: [e.message])
            return redirect(action: 'index')
        }
    }

    /**
     * TODO supply meaningful data
     * @return
     */
    private String whoIs() {
        return 'all'
    }

    /**
     * Invoked when the 'Submit' button is clicked on a form.
     *
     * TODO this method needs a refactor, can we move some/most of the logic into the service?
     *
     */
    def next = {
        log.debug "next: $params"
        FormInstance instance = formDataService.checkInstance(params.instanceId as Long)
        if (!instance) {
            flash.message = message(code:"goodform.form.invalid", args: [params.instanceId])
            return redirect(action: 'apply')
        }

        Map currentFormData = formDataService.cleanUpStateParams(params)
        Form form = formDataService.getFormQuestions(instance.formVersion)
        List asked = formDataService.getSubset(instance.storedCurrentQuestion(), form)

        //todo re look at this merged Data as we should merge after validate? Also multiple calls to storedFormData
        Map mergedFormData = rulesEngineService.cleanUpJSONNullMap(instance.storedFormData()) << currentFormData
        mergedFormData.formVersion = instance.formVersion

        instance.storedCurrentQuestion().each { ref ->
            if (mergedFormData[ref]?.recheck) {
                (mergedFormData[ref] as Map).remove('recheck')
            }
        }

        boolean error = false
        asked.each { Question question ->
            //note the or error here makes sure error isn't reset whilst checking all form elements (so don't move it in front :-)
            error = formDataService.validateAndProcessFields(question.formElement, mergedFormData, instance) || error
        }
        instance.storeFormData(mergedFormData)

        if (!error) {
            try {
                Map processedFormData = formDataService.processNext(instance, mergedFormData)
                log.debug "next processedFormData: ${processedFormData.toString(2)}"
                //rules engine returns "End" as the next question at the end
                if (processedFormData.next.size() == 1 && processedFormData.next[0] == 'End') {
                    return redirect(action: 'endForm', id: instance.id)
                }
                List<Question> current = formDataService.getSubset(processedFormData.next, form)
                List answered = formDataService.getAnsweredQuestions(instance, form)
                render(view: '/form/formDetails', model: [form: form, asked: answered, questions: current, formData: processedFormData, instance: instance])
            } catch (RulesEngineException e) {
                //logged in processNext just set the flash message and redirect
                flash.message = message(code:"goodform.rules.error", args: [e.message])
                return redirect(action: 'apply')
            }
        } else {
            //error detected, redisplay form
            List answered = formDataService.getAnsweredQuestions(instance, form)
            render(view: '/form/formDetails', model: [form: form, asked: answered, questions: asked, formData: mergedFormData, instance: instance])
        }
    }

    /**
     *
     */
    def back = {
        log.debug "back: $params"
        FormInstance instance = formDataService.checkInstance(params.id as Long)
        if (!instance) {
            flash.message = message(code:"goodform.form.invalid", args: [params.id])
            return redirect(action: 'apply')
        }

        List state = instance.storedState()
        List currentQ = state.reverse()[params.qset as int]
        instance.storeCurrentQuestion(currentQ)
        instance.storeState(formDataService.truncateState(state, currentQ))
        instance.save(flush: true)
        redirect(action: 'continueApp', id: instance.id)
    }

    /**
     * Handles the final submission of a form.
     */
    def endForm = {
        log.debug "end: $params"
        FormInstance instance = formDataService.checkInstance(params.id as Long)
        if (!instance) {
            flash.message = message(code:"goodform.form.invalid", args: [params.id])
            return redirect(action: 'apply')
        }
        Map formData = instance.storedFormData()
        JSONObject processedJSONFormData = rulesEngineService.ask('CheckRequiredDocuments', formData)
        formData = rulesEngineService.cleanUpJSONNullMap(processedJSONFormData)
        updateStoredFormInstance(instance, formData)
        log.debug "end FormData: ${(formData as JSON).toString(true)}"
        //[instance: instance, formData: formData]
        render(view: '/form/endForm', model: [instance: instance, formData: formData])
    }

    /**
     *
     * @param instance
     * @param processedFormData
     * @return
     */
    private updateStoredFormInstance(FormInstance instance, Map processedFormData) {
        List state = instance.storedState()
        def nextInState = state.find { s ->
            s == processedFormData.next
        }
        if (!nextInState) {
            state.add(processedFormData.next)
            instance.storeState(state)
        }
        instance.storeCurrentQuestion(processedFormData.next)
        instance.storeFormData(processedFormData)
    }

    /**
     * Displays a read-only view of a form.
     */
    def view = {
        log.debug "view: $params"
        FormInstance instance = formDataService.checkInstance(params.id as Long)
        if (!instance) {
            flash.message = message(code:"goodform.form.invalid", args: [params.id])
            return redirect(action: 'apply')
        }

        Map formData = instance.storedFormData()
        log.debug "view FormData: ${(formData as JSON).toString(true)}"
        return [instance: instance, formData: formData]

    }

    /**
     *
     */
    def submit = {
        log.debug "submitForm: $params"
        FormInstance instance = formDataService.checkInstance(params.id as Long)
        if (!instance) {
            flash.message = message(code:"goodform.form.invalid", args: [params.id])
            return redirect(action: 'apply')
        }

        Map result = applicationService.submitFormInstance(instance)
        if (result.errors.isEmpty()) {

            flash.message = "Form Instance submitted: File number $instance.fileNumber"
            return redirect(action: 'apply')
        } else {
            flash.message = "Form Instance had errors $result.errors"
            return redirect(action: 'apply')
        }
    }
}
