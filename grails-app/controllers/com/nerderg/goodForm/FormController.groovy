package com.nerderg.goodForm

import com.nerderg.goodForm.form.Form

import com.nerderg.goodForm.form.Question
import net.sf.json.JSONObject
import grails.converters.JSON

/**
 *
 */
class FormController {

    def pdfRenderingService

    def formDataService

    def goodFormService

    def rulesEngineService

    def applicationService

    /**
     * Creates a new form instance.
     *
     * Pre conditions:
     * <ul>
     * <li>FormDefinition must be defined in the database (typically in Bootstrap).</li>
     * </ul>
     * TODO retrieve form name from input parameters and redirect to error page if not supplied
     * TODO: redirect to error page when FormDefinition not supplied
     *
     */
    def newForm = {
        log.debug "apply: $params"
        try {
            String formName = params.formName
            //TODO handle no form name
            Form form = formDataService.getForm()
            Map formData = rulesEngineService.ask(formName, [loginType: whoIs()])
            FormInstance application = formDataService.createApplication(formData)
            List ask = formDataService.getSubset(formData.next, form)
            render(view: 'jobApplication', model: [form: form, asked: [], questions: ask, formData: formData, app: application])
        } catch (RulesEngineException e) {
            flash.message = "An error occured during rules processing: '$e.message'."
            //            //TODO redirect somewhere relevant
            return redirect(controller: 'jobApplication', action: 'index')
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
    def applyNext = {
        log.debug "applyNext: $params"
        FormInstance application = formDataService.checkApplication(params.applicationId as Long)
        if (!application) {
            flash.message = "Something has gone wrong, I can't find that Application (${params.id})"
            return redirect(action: 'apply')
        }

        Map currentFormData = formDataService.cleanUpStateParams(params)
        Form form = formDataService.getFormQuestions(application.formVersion)
        List asked = formDataService.getSubset(application.storedCurrrentQuestion(), form)

        //todo re look at this merged Data as we should merge after validate? Also multiple calls to storedFormData
        Map mergedFormData = rulesEngineService.cleanUpJSONNullMap(application.storedFormData()) << currentFormData
        mergedFormData.formVersion = application.formVersion

        application.storedCurrrentQuestion().each { ref ->
            if (mergedFormData[ref]?.recheck) {
                (mergedFormData[ref] as Map).remove('recheck')
            }
        }

        boolean error = false
        asked.each { Question question ->
            //note the or error here makes sure error isn't reset whilst checking all form elements (so don't move it in front :-)
            error = formDataService.validateAndProcessFields(question.formElement, mergedFormData, application) || error
        }
        application.storeFormData(mergedFormData)

        if (!error) {
            try {
                Map processedFormData = formDataService.processNext(application, mergedFormData)
                log.debug "applyNext processedFormData: ${processedFormData.toString(2)}"
                //rules engine returns "End" as the next question at the end
                if (processedFormData.next.size() == 1 && processedFormData.next[0] == 'End') {
                    return redirect(action: 'endForm', id: application.id)
                }
                List<Question> current = formDataService.getSubset(processedFormData.next, form)
                List answered = formDataService.getAnsweredQuestions(application, form)
                render(view: 'jobApplication', model: [form: form, asked: answered, questions: current, formData: processedFormData, app: application])
            } catch (RulesEngineException e) {
                //logged in processNext just set the flash message and redirect
                flash.message = "Rule error $e.message."
                return redirect(action: 'apply')
            }
        } else {
            //error detected, redisplay form
            List answered = formDataService.getAnsweredQuestions(application, form)
            render(view: 'jobApplication', model: [form: form, asked: answered, questions: asked, formData: mergedFormData, app: application])
        }
    }

    /**
     *
     */
    def goback = {
        log.debug "goback: $params"
        FormInstance application = formDataService.checkApplication(params.id as Long)
        if (!application) {
            flash.message = "Something has gone wrong, I can't find that Application (${params.id})"
            return redirect(action: 'apply')
        }

        List state = application.storedState()
        List currentQ = state.reverse()[params.qset as int]
        application.storeCurrentQuestion(currentQ)
        application.storeState(formDataService.truncateState(state, currentQ))
        application.save(flush: true)
        redirect(action: 'continueApp', id: application.id)
    }

    /**
     * TODO I think this might be only needed for legal aid, might be able to remove this from example
     */
    def endForm = {
        log.debug "endForm: $params"
        FormInstance application = formDataService.checkApplication(params.id as Long)
        if (!application) {
            flash.message = "Something has gone wrong, I can't find that Application (${params.id})"
            return redirect(action: 'apply')
        }
        Map formData = application.storedFormData()
        JSONObject processedJSONFormData = rulesEngineService.ask('CheckRequiredDocuments', formData)
        formData = rulesEngineService.cleanUpJSONNullMap(processedJSONFormData)
        updateStoredApplication(application, formData)
        log.debug "end FormData: ${(formData as JSON).toString(true)}"
        [app: application, formData: formData]
    }

    /**
     *
     * @param application
     * @param processedFormData
     * @return
     */
    private updateStoredApplication(FormInstance application, Map processedFormData) {
        List state = application.storedState()
        def nextInState = state.find { s ->
            s == processedFormData.next
        }
        if (!nextInState) {
            state.add(processedFormData.next)
            application.storeState(state)
        }
        application.storeCurrentQuestion(processedFormData.next)
        application.storeFormData(processedFormData)
    }

    /**
     *
     */
    def view = {
        log.debug "view: $params"
        FormInstance application = formDataService.checkApplication(params.id as Long)
        if (!application) {
            flash.message = "Something has gone wrong, I can't find that Application (${params.id})"
            return redirect(action: 'apply')
        }

        Map formData = application.storedFormData()
        log.debug "view FormData: ${(formData as JSON).toString(true)}"
        if (params.name) {
            return renderPdf([template: 'applicationView', model: [app: application, formData: formData]])
        } else {
            return [app: application, formData: formData]
        }
    }

    def submit = {
        log.debug "submitForm: $params"
        FormInstance application = formDataService.checkApplication(params.id as Long)
        if (!application) {
            flash.message = "Something has gone wrong, I can't find that Application (${params.id})"
            return redirect(action: 'apply')
        }

        //TODO should this go into applicationService.submitApplication?
        //write pdf of application form to the applications attachments directory for attachment to the file history
        Map formData = application.storedFormData()
        //TODO create example-app-specific grails config entries to store pdfs
        File location = new File(grailsApplication.config.uploaded.file.location.toString() + 'applications/' + application.id)
        location.mkdirs()
        File upload = new File(location, "jobApplication.pdf")
        upload.withOutputStream {outputStream ->
            pdfRenderingService.render([controller: 'grant', template: 'applicationView', model: [app: application, formData: formData]], outputStream)
        }

        Map result = applicationService.submitApplication(application)
        if (result.errors.isEmpty()) {

            flash.message = "Application submitted: File number $application.fileNumber"
            return redirect(action: 'apply')
        } else {
            flash.message = "Application had errors $result.errors"
            return redirect(action: 'apply')
        }
    }
}
