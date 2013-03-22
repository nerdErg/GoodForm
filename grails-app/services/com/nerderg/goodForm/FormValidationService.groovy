package com.nerderg.goodForm

import com.nerderg.goodForm.form.FormElement
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

/**
 * Contains custom validators for form elements. This class should be subclassed to include project-specific validation
 * logic within a project using GoodForms.
 *
 * Projects using Goodforms can add a custom validation service by executing the following as part of the Bootstrap:
 *
 * <pre>
 * def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT)
 * ctx.formDataService.addValidator({formElement, fieldValue -> ctx.customValidationService.validateSomeField(formElement, fieldValue)})
 * </pre>
 */
class FormValidationService {

    def g = new ValidationTagLib()

    static transactional = true

    def customValidationMap = [:]

    def addCustomValidator(String validationName, Closure closure) {
        customValidationMap.put(validationName, closure)
    }

    def hasError(FormElement formElement, String fieldValue) {
        def validationName = formElement.attr.validate
        Closure validator = customValidationMap[validationName]
        if (validator) {
            return validator(formElement, fieldValue)
        } else {
            throw new FormValidatorMissingException("No validator called $validationName found. Add it using formValidationService.addCustomValidator()")
        }
    }


    /**
     * Invokes the validation method corresponding the the 'validate' attribute.
     */
    def customValidation = {FormElement formElement, String fieldValue ->
        def error = false
        if (fieldValue && formElement.attr.containsKey('validate') && hasError(formElement, fieldValue)) {
            formElement.attr.error += g.message(code: "goodform.validate." + formElement.attr.validate + ".invalid")
            error = true
        }
        return error
    }
}

class FormValidatorMissingException extends Exception {
    def FormValidatorMissingException() {
        super()
    }

    def FormValidatorMissingException(String message) {
        super(message)
    }

    def FormValidatorMissingException(String message, Throwable cause) {
        super(message, cause)
    }

    def FormValidatorMissingException(Throwable cause) {
        super(cause)
    }

}