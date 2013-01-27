package com.nerderg.goodForm

import com.nerderg.goodForm.form.FormElement
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

/**
 * Contains custom validators for form elements. The validators included in this class validate postcodes and phone numbers
 * conform to Australian standards.
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

    def hasError(FormElement formElement, String fieldValue) {
        def validationName = formElement.attr.validate
        return "$validationName"(formElement, fieldValue)
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
