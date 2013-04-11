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

    private customValidationMap = [:]

    void addCustomValidator(String validationName, Closure closure) {
        customValidationMap.put(validationName, closure)
    }

    /**
     *
     * @param formElement
     * @param fieldValue
     * @return
     */
    boolean hasError(FormElement formElement, String fieldValue) {
        def validationName = formElement.attr.validate
        Closure validator = customValidationMap[validationName]
        if (!validator) {
            throw new FormValidatorMissingException("No validator called $validationName found. Add it using formValidationService.addCustomValidator()")
        }
        return validator(formElement, fieldValue)
    }

    /**
     * Invokes the validation method corresponding the the 'validate' attribute.
     */
    def customValidation = { FormElement formElement, String fieldValue ->
        boolean error = false
        if (fieldValue && formElement.attr.containsKey('validate') && hasError(formElement, fieldValue)) {
            String code = "goodform.validate." + formElement.attr.validate + ".invalid"
            def request = RequestContextHolder.requestAttributes?.request
            def locale = request ? RequestContextUtils.getLocale(request) : Locale.default
            formElement.attr.error += messageSource.getMessage(code, null, code, locale) ?: code
            error = true
        }
        return error
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
