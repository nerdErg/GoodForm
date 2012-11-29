package com.nerderg.goodForm

import com.nerderg.goodForm.form.FormElement

/**
 *
 */
class FormValidationService {

    def addressWranglingService

    static transactional = true

    def validate(String validationName, String fieldValue) {
        "$validationName"(fieldValue)
    }

    def postcode(postcode) {
        return addressWranglingService.isValidPostcode(postcode)
    }

    def validatePostcode(FormElement formElement, String fieldValue) {
        def error = false
        if (fieldValue && formElement.attr.containsKey('validate')) {
            if (!validate(formElement.attr.validate, fieldValue)) {
                //TODO i18n
                formElement.attr.error += "The postcode is not valid. Please check it."
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
    def validatePhone(FormElement formElement, String fieldValue) {
        def error = false
        if (fieldValue && formElement.attr.containsKey('phone')) {
            String numbers = fieldValue.replaceAll(/[^0-9\+]/, '')
            if (numbers.size() < 8) {
                formElement.attr.error += g.message(code: "goodform.validate.phone.minLength")
                error = true
            }
            if (!(numbers =~ /^(\+|02|03|04|07|08|[2-9])/)) {
                //TODO i18n
                formElement.attr.error += "The prefix for this phone number looks wrong (e.g. can't be 00xx, 01xx, 05xx, 06xx, 09xx, or 1xxx)"
                error = true
            }
        }
        return error
    }

}
