package com.nerderg.goodForm

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.support.RequestContextUtils

import com.nerderg.goodForm.form.FormElement

/**
 * Contains custom validators for form elements. This class should be subclassed to include project-specific validation
 * logic within a project using GoodForms.
 *
 * Projects using Goodforms can add a custom validation service by executing the following as part of the Bootstrap:
 *
 * <pre>
 * def customValidationService
 * def formDataService
 *
 * def init = { servletContext ->
 *    formDataService.addValidator({formElement, fieldValue -> customValidationService.validateSomeField(formElement, fieldValue)})
 * }
 * </pre>
 *
 * @author Peter McNeil
 * @author Ross Rowe
 */
class FormValidationService {

    static transactional = false

    def messageSource

    private Map<String,Closure> customValidationMap = [:]

    void addCustomValidator(String validationName, Closure closure) {
        customValidationMap.put(validationName, closure)
    }

    /**
     *
     * @param formElement
     * @param fieldValue
     * @return
     */
    boolean hasError(FormElement formElement, Map formData, fieldValue, Integer index) {
        String validationName = formElement.attr.validate
        Closure validator = customValidationMap[validationName]
        if (!validator) {
            throw new FormValidatorMissingException("No validator called $validationName found. Add it using formValidationService.addCustomValidator()")
        }
        return validator(formElement, formData, fieldValue, index)
    }

    /**
     * Invokes the validation method corresponding the the 'validate' attribute.
     */
    Closure customValidation = { FormElement formElement, Map formData, fieldValue, Integer index ->
        boolean error = false
        if (fieldValue && formElement.attr.containsKey('validate') && hasError(formElement, formData, fieldValue, index)) {
            String code = "goodform.validate." + formElement.attr.validate + ".invalid"
            appendError(formElement, formData, code, index, [fieldValue])
            error = true
        }
        return error
    }

    void appendError(FormElement formElement, Map formData, String code, Integer index, List args = null) {
        String message = message(code, args)
        String name = formElement.attr.name + (index == null ? '' : index)
        formData.fieldErrors[name]
        if(formData.fieldErrors[name]) {
            formData.fieldErrors[name] += " \n$message"
        } else {
            formData.fieldErrors[name] = message
        }
    }

    private String message(String code, List args = null) {
        def request = RequestContextHolder.requestAttributes?.request
        def locale = request ? RequestContextUtils.getLocale(request) : Locale.default
        messageSource.getMessage(code, args ? (args as Object[]) : null, code, locale) ?: code
    }

}

/**
 *
 * @author Peter McNeil
 */
class FormValidatorMissingException extends Exception {
    FormValidatorMissingException() {
        super()
    }

    FormValidatorMissingException(String message) {
        super(message)
    }

    FormValidatorMissingException(String message, Throwable cause) {
        super(message, cause)
    }

    FormValidatorMissingException(Throwable cause) {
        super(cause)
    }
}
