package com.nerderg.goodForm

import com.nerderg.goodForm.form.Form
import com.nerderg.goodForm.form.FormElement
import com.nerderg.goodForm.form.Question
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.util.WebUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.RequestContextUtils

import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import javax.annotation.PostConstruct

/**
 * Handles processing and validating form data.
 *
 * @author Peter McNeil
 */
class FormDataService {

    static transactional = false

    def goodFormService
    def formReferenceService
    def rulesEngineService
    def grailsApplication
    def messageSource

    def springSecurityService

    /**
     * Handles custom form validation
     */
    def formValidationService

    Map<Long, Form> forms = [:]

    /**
     * Performs validation of date form elements.
     *
     * @param fieldValue
     * @param formElement
     * @return
     */
    Closure validateDate = { FormElement formElement, fieldValue ->
        boolean error = false
        if (fieldValue && formElement.attr.containsKey('date')) {
            try {
                if (!isLegalDate(formElement.attr.date, fieldValue)) {
                    error = true
                    formElement.attr.error += message("goodform.validate.date.invalid")
                } else {
                    Date d = Date.parse(formElement.attr.date, fieldValue)
                    if (formElement.attr.max) {
                        if (formElement.attr.max == 'today') {
                            if (d.time > System.currentTimeMillis()) {
                                formElement.attr.error += message("goodform.validate.date.future")
                                error = true
                            }
                        } else {
                            Date max = Date.parse(formElement.attr.date, formElement.attr.max)
                            if (d.time > max.time) {
                                formElement.attr.error += message("goodform.validate.date.greaterThan", [formElement.attr.max])
                                error = true
                            }
                        }
                    }
                    if (formElement.attr.min) {
                        Date min = Date.parse(formElement.attr.date, formElement.attr.min)
                        if (d.time < min.time) {
                            formElement.attr.error += message("goodform.validate.date.lessThan", [formElement.attr.min])
                            error = true
                        }
                    }
                }
            } catch (ParseException e) {
                formElement.attr.error += message("goodform.validate.date.invalid")
                error = true
            }
        }
        return error
    }

    boolean isLegalDate(String format, String text) {
        SimpleDateFormat sdf = new SimpleDateFormat(format)
        sdf.setLenient(false)
        return sdf.parse(text, new ParsePosition(0)) != null
    }

    /**
     * Validates that a field value matches a defined regex pattern.
     *
     * @param fieldValue
     * @param formElement
     * @return
     */
    Closure validatePattern = { FormElement formElement, fieldValue ->
        boolean error = false
        if (fieldValue && formElement.attr.containsKey('pattern')) {
            String pattern
            String message = message("goodform.validate.invalid.pattern")
            if (formElement.attr.pattern instanceof List) {
                pattern = formElement.attr.pattern[0]
                if (formElement.attr.pattern.size() > 1) {
                    message = formElement.attr.pattern[1]
                }
            } else {
                pattern = formElement.attr.pattern
            }
            if (fieldValue && !(fieldValue ==~ pattern)) {
                formElement.attr.error += message
                error = true
            }
        }
        return error
    }

    /**
     * Validates that a required field is present.
     *
     * @param formElement
     * @param fieldValue
     * @return
     */
    Closure validateMandatoryField = { FormElement formElement, fieldValue ->
        boolean error = false
        if (formElement.attr.containsKey('required') && (fieldValue == null || fieldValue == '')) {
            formElement.attr.error += message("goodform.validate.required.field")
            error = true
        }
        return error
    }

    /**
     * Adds the mandatory field, date, pattern and generic custom validators to the initial list of validators
     * that process field data.  Validators can be explicitly added by invoking the {@link #addValidator} method.
     */
    List<Closure> validators = [validateMandatoryField, validateDate, validatePattern]

    @PostConstruct
    void initializeValidators() {
        addValidator(formValidationService.customValidation)
    }

    void addValidator(Closure closure) {
        validators.add(closure)
    }

    Form getForm(String formName) {
        return getFormQuestions(formCurrentDefinitionForName(formName))
    }

    /**
     *
     * @return the FormDefinition with a name equal to <code>formName</code> that has the max formVersion value
     */
    FormDefinition formCurrentDefinitionForName(String formName) {
        FormDefinition.executeQuery(
                "select f from FormDefinition f where name = :formName order by f.formVersion desc",
                [formName: formName, max: 1])[0]
    }

    Form getFormQuestions(FormDefinition formDefinition) {
        if (!formDefinition) {
            return null
        }
        String key = formDefinition.name + formDefinition.formVersion
        if (!forms[key]) {
            Form form = createForm(formDefinition)
            forms[key] = form
        }
        return forms[key]
    }

    Form createForm(FormDefinition formDefinition) {
        Form form = goodFormService.compileForm(formDefinition.formDefinition)
        form.version = formDefinition.formVersion
        form.name = formDefinition.name
        form.formDefinitionId = formDefinition.id
        return form
    }

    FormInstance getFormInstance(Long id) {
        return FormInstance.get(id)
    }

    /**
     * @param state
     * @return
     */
    Map cleanUpStateParams(Map state) {
        // params are broken into maps for each question
        // first remove all thing.thing.thing values because we only want to base values or the maps
        Map intermediate = state.findAll {
            it.key.indexOf('.') < 0
        }
        // now do the same for all the map values (recursively)
        intermediate.each {
            if (it.value instanceof Map) {
                it.value = cleanUpStateParams(it.value)
            }
        }
        return intermediate
    }

    //todo break this into validate fields and process fields
    /**
     * Process the returned field values by first validating the field then getting references, saving attachments
     * and finally converting to typed fields from string (mainly numeric fields)
     *
     * Numeric fields number and money are converted to BigDecimal
     *
     * @param formElement
     * @param formData the current data Map
     * @param instance the FormInstance object
     * @return true on error
     */
    boolean validateAndProcessFields(FormElement formElement, Map formData, FormInstance instance) {
        //note makeElement name uses the attr.name of it's parent so it must be set. (side effect)
        boolean error = false

        if (formElement.attr.heading) {
            return error // ignore headings
        }

        formElement.attr.name = goodFormService.makeElementName(formElement)
        formElement.attr.error = ""

        def fieldValue = goodFormService.findField(formData, formElement.attr.name)

        if (fieldValue instanceof String[]) {
            fieldValue.each { String fv ->
                error = validateField(formElement, fv, error)
            }
        } else if (fieldValue instanceof MultipartFile) {
            error = validateField(formElement, fieldValue.getName(), error)
        } else if (fieldValue == null || fieldValue instanceof String) {
            error = validateField(formElement, fieldValue, error)
        }

        //get references and store in the formData
        handleReferences(formElement, fieldValue, formData)

        //get attached file and store it, save the reference to it in the formData
        handleAttachment(formElement, instance, formData)

        //handle subElements
        error = handleSubElements(formElement, formData, instance, error)

        //convert numeric fields to bigdecimal
        error = checkAndConvertFieldToBigDecimal(fieldValue, formElement, formData, error)

        return error
    }

    private void handleReferences(FormElement formElement, fieldValue, Map formData) {
        if (formElement.attr.containsKey('ref')) {
            //references are stored at the question level under the reference name so there is a limitation there
            def ref = formReferenceService.lookupReference(formElement.attr.ref, fieldValue)
            if (ref) {
                formData[formElement.attr.name.split(/\./)[0]]."${formElement.attr.ref}" = ref
            }
        }
    }

    private void handleAttachment(FormElement formElement, FormInstance instance, Map formData) {
        if (formElement.attr.containsKey('attachment')) {
            //get the uploaded file and store somewhere
            def grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
            def f = grailsWebRequest.getCurrentRequest().getFile(formElement.attr.name)
            if (f && !f.empty) {
                String basedir = grailsApplication.config.uploaded.file.location.toString() + 'applications/' + instance.id
                File location = new File(basedir)
                location.mkdirs()
                List<String> fieldSplit = formElement.attr.name.split(/\./)
                String filename = "${fieldSplit[0]}.${fieldSplit.last()}-${f.getOriginalFilename()}"
                File upload = new File(location, filename)
                f.transferTo(upload)
                goodFormService.setField(formData, formElement.attr.name, upload.name)
            } else {
                //todo refactor so we don't continually get the stored FormData also dangerous for overwrite
                def existingFile = goodFormService.findField(instance.storedFormData(), formElement.attr.name)
                if (existingFile) {
                    goodFormService.setField(formData, formElement.attr.name, existingFile)
                } else {
                    goodFormService.setField(formData, formElement.attr.name, 'none')
                }
            }
        }
    }

    private boolean handleSubElements(FormElement formElement, Map formData, FormInstance instance, boolean error) {
        if (formElement.attr.containsKey('each')) {
            //handle 'each' which dynamically adds elements
            goodFormService.processEachFormElement(formElement, formData) { Map subMap ->
                error = validateAndProcessFields(subMap.element, formData, instance) || error
            }
        } else {
            formElement.subElements.each { FormElement sub ->
                error = validateAndProcessFields(sub, formData, instance) || error
            }
        }
        return error
    }

    private boolean checkAndConvertFieldToBigDecimal(fieldValue, FormElement formElement, Map formData, boolean error) {
        try {
            if (fieldValue && (formElement.attr.containsKey('number') || formElement.attr.containsKey('money'))) {
                log.debug "converting ${formElement.attr.name} value ${fieldValue} to bigdecimal"
                if (fieldValue instanceof String[] || fieldValue instanceof List) {
                    goodFormService.setField(formData, formElement.attr.name.toString(), fieldValue.collect {
                        if (it) {
                            it as BigDecimal
                        }
                    })
                } else {
                    goodFormService.setField(formData, formElement.attr.name.toString(), fieldValue as BigDecimal)
                }
            }
        } catch (NumberFormatException e) {
            log.error "${e.message} converting $fieldValue to number"
            //todo i18n
            formElement.attr.error += "$fieldValue isn't a number."
            error = true
        }
        return error
    }

    /**
     * Retrieves and invokes the validators that have been added for the formElement.
     *
     * @param formElement
     * @param fieldValue
     * @param error
     * @return true if the field contains errors, false if not
     */
    boolean validateField(FormElement formElement, fieldValue, boolean error) {

        //iterate over validators
        validators.each { Closure validator ->
            error = validator(formElement, fieldValue) || error
        }
        return error
    }

    /**
     *
     * Get the questions that have been answered so far up to the current question set
     * @param instance
     * @param questions
     * @return answered questions
     */
    def getAnsweredQuestions(FormInstance instance, Form form) {

        List answered = []
        List state = instance.storedState()
        List currentQuestions = instance.storedCurrentQuestion()

        def i = 0
        List qSet
        while (i < state.size() && (qSet = state[i++] as List) != currentQuestions) {
            goodFormService.withQuestions(qSet, form) { q, qRef ->
                answered.add(q)
            }
        }
        return answered
    }

    String getCurrentUser() {
        if (springSecurityService) {
            if (springSecurityService.principal instanceof String) {
                springSecurityService.principal
            } else {
                springSecurityService.principal.username
            }
        } else {
            'unknown'
        }
    }

    FormInstance createFormInstance(Form form, Map formData) {
        // ordinarily not a good idea, but this is the only method that writes to the database
        FormInstance.withTransaction {
            FormInstance instance = new FormInstance(
                    started: new Date(), userId: getCurrentUser(), instanceDescription: form.name,
                    currentQuestion: formData.next.last(), formDefinitionId: form.formDefinitionId)
            instance.storeFormData(formData)
            instance.storeState([formData.next])
            instance.storeCurrentQuestion(formData.next)
            instance.formVersion = form.version
            instance.formDefinitionId = form.formDefinitionId
            instance.readOnly = false
            instance.save()
            return instance
        }
    }

    /**
     *
     * @param refs
     * @param form
     * @return
     */
    List<Question> getSubset(Collection refs, Form form) {
        List<Question> questions = []
        refs.each {
            Question q = form[it]
            if (q) {
                questions.add(q)
            } else {
                log.error "Question $it not found."
                throw new FormDataException("Question $it not found.")
            }
        }
        return questions
    }

    /**
     *
     * Process the form data through the rules engine. The rules engine returns the next set of questions to be asked.
     * We check through the existing form data to see if all the next set of questions have been answered and if so we ask
     * the rules engine to check the next set until we find a question in a set that hasn't been answered already.
     *
     * This way we skip forward through the questions that have been answered to only ask relevant questions
     *
     * One side effect is that we set the flash.message on the way through,perhaps we shouldn't
     * @param instance
     * @param mergedFormData
     * @return processedFormData up to the next un-asked question
     */
    Map processNext(FormInstance instance, Map mergedFormData) {
        String lastQuestion = instance.storedCurrentQuestion().last()
        FormDefinition definition = FormDefinition.get(instance.formDefinitionId)
        String ruleName = definition.name + lastQuestion
        mergedFormData.remove('next')  //prevent possible pass through by rules engine
        try {
            JSONObject processedJSONFormData = rulesEngineService.ask(ruleName, mergedFormData) as JSONObject
            def processedFormData = cleanUpJSONNullMap(processedJSONFormData)

            if (processedFormData[lastQuestion].message) {
                //TODO how to handle adding message into flash?
                //flash.message = processedFormData[lastQuestion].message
            }

            updateStoredFormInstance(instance, processedFormData)

            if (processedFormData.next.size() == 1 && processedFormData.next[0] == 'End') {
                return processedFormData
            }
            //prevent loops if rules engine sends you back to the same questions
            if (processedFormData.next.contains(lastQuestion)) {
                return processedFormData
            }

            //search for answers to the next questions - if we don't have an answer we ask this question set
            for (String q in processedFormData.next) {
                if (!processedFormData[q] || processedFormData[q].recheck) {
                    return processedFormData
                }
            }
            //otherwise we check the next lot
            return processNext(instance, processedFormData)
        } catch (RulesEngineException e) {
            log.error "Calling rule $ruleName, instance $instance.id: $e"
            throw e
        }
    }

    def updateStoredFormInstance(FormInstance instance, Map processedFormData) {

        List state = instance.storedState()
        def nextInState = state.find { s ->
            s == processedFormData.next
        }
        if (!nextInState) {
            state.add(processedFormData.next)
            instance.storeState(state)
        }
        if (processedFormData.description) {
            instance.instanceDescription = processedFormData.description
        }
        instance.storeCurrentQuestion(processedFormData.next)
        instance.storeFormData(processedFormData)
    }

    /**
     * Remove all the state after the currentQuestion list of questions.
     * If the current question doesn't exist don't truncate anything
     * @param state
     * @param currentQuestion
     * @return A new truncated list
     */
    def List truncateState(List<List> state, List currentQuestion) {
        List trunkState = []
        int i = 0
        List s = state[i]
        boolean found = false
        while (i < state.size() && !found) {
            trunkState.add(s)
            found = (s == currentQuestion)
            s = state[++i]
        }
        return trunkState
    }

    Map cleanUpJSONNullMap(Map m) {
        m.each {
            if (it.value.equals(null)) {
                it.value = null
            } else if (it.value instanceof Map) {
                it.value = cleanUpJSONNullMap(it.value)
            } else if (it.value instanceof Collection) {
                it.value = cleanUpJSONNullCollection(it.value)
            }
        }
    }

    Collection cleanUpJSONNullCollection(Collection c) {
        //create a new collection sans JSONObject.Null objects
        List collect = []
        c.each { v ->
            if (!v.equals(null)) {
                if (v instanceof Collection) {
                    collect.add(cleanUpJSONNullCollection(v))
                } else if (v instanceof Map) {
                    collect.add(cleanUpJSONNullMap(v))
                } else {
                    collect.add(v)
                }
            } else {
                collect.add(null)
            }
        }
        return collect
    }

    private String message(String code, List args = null) {
        def request = RequestContextHolder.requestAttributes?.request
        def locale = request ? RequestContextUtils.getLocale(request) : Locale.default
        messageSource.getMessage(code, args ? (args as Object[]) : null, code, locale) ?: code
    }

    /**
     * Retrieves the list of {@link FormInstance} records stored for a specific {@link FormDefinition}.
     *
     * @param formDefinitionId the form definition id
     * @return list of matching {@link FormInstance}s
     */
    List<FormInstance> getForms(Long formDefinitionId) {
        FormInstance.findAllByFormDefinitionId(formDefinitionId)
    }

    /**
     * Retrieves a {@link FormDefinition} instance for the given id.
     * @param id
     * @return
     */
    FormDefinition getFormDefinition(Long id) {
        FormDefinition.get(id)
    }

    /**
     * Retrieves a {@link FormInstance} instance for the given id.
     * @param id
     * @return
     */
    FormInstance getForm(Long id) {
        FormInstance.get(id)
    }

    /**
     * Retrieves the latest version of each unique form definition.
     * @return
     */
    List<FormDefinition> getLatestFormDefinitions() {
        return FormDefinition.executeQuery("select f from FormDefinition f where f.formVersion = (select max(g.formVersion) from FormDefinition g where g.name = f.name)")
    }

    FormDefinition createFormDefinition(Long id, String formDefinition) {
        FormInstance.withTransaction {
            //find FormDefinition for id
            FormDefinition existingFormDefinition = FormDefinition.get(id)
            //Create new FormDefinition
            FormDefinition newFormDefinition = new FormDefinition(
                    name: existingFormDefinition.name,
                    formDefinition: formDefinition,
                    formVersion: existingFormDefinition.formVersion + 1
            )

            if (newFormDefinition.validate()) {
                newFormDefinition.save()
            } else {
                //todo throw a nicer exception and more meaningful message since most apps will have the default don't fail on save :-/
                newFormDefinition.errors.each {
                    println it
                }
                throw new Exception("failed to save new form definition")
            }
        }
    }
}

/**
 * Custom exception thrown when errors are detected in the processing of form definitions.
 *
 * @author Peter McNeil
 */
class FormDataException extends Exception {
    FormDataException() {
        super()
    }

    FormDataException(String message) {
        super(message)
    }

    FormDataException(String message, Throwable cause) {
        super(message, cause)
    }

    FormDataException(Throwable cause) {
        super(cause)
    }
}
