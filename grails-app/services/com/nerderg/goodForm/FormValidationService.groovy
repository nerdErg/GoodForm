package com.nerderg.goodForm

import com.nerderg.goodForm.form.FormElement
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

/**
 * Contains custom validators for form elements.
 *
 * Projects using Goodforms can add a custom validation service by executing the following as part of the Bootstrap:
 *
 * <pre>
 * def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT)
 * ctx.formDataService.addValidator({formElement, fieldValue -> ctx.customValidationService.validateSomeField(formElement, fieldValue)})
 * </pre>
 */
class FormValidationService {

    def addressWranglingService

    def g = new ValidationTagLib()

    static transactional = true

    def validate(String validationName, String fieldValue) {
        "$validationName"(fieldValue)
    }

    def postcode(postcode) {
        return addressWranglingService.isValidPostcode(postcode)
    }

    def validatePostcode = {FormElement formElement, String fieldValue ->
        def error = false
        if (fieldValue && formElement.attr.containsKey('validate')) {
            if (!validate(formElement.attr.validate, fieldValue)) {
                formElement.attr.error += g.message(code: "goodform.validate.postcode.invalid")
                error = true
            }
        }
        return error
    }

    /**
     *
     * @param formElement
     * @param fieldValue
     * @return
     */
    def validatePhone = {FormElement formElement, String fieldValue ->
        def error = false
        if (fieldValue && formElement.attr.containsKey('phone')) {
            String numbers = fieldValue.replaceAll(/[^0-9\+]/, '')
            if (numbers.size() < 8) {
                formElement.attr.error += g.message(code: "goodform.validate.phone.minLength")
                error = true
            }
            //TODO store phone prefix in properties somewhere?
            if (!(numbers =~ /^(\+|02|03|04|07|08|[2-9])/)) {
                formElement.attr.error += g.message(code: "goodform.validate.phone.invalid")
                error = true
            }
        }
        return error
    }

}
