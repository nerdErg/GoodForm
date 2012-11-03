package com.nerderg.goodForm

import grails.util.GrailsUtil

/**
 * Persists the form definition in the database.
 *
 * Copied from GrantForm
 */
class FormDefinition {

    String formDefinition

    static mapping = {
        if (GrailsUtil.getEnvironment() != 'test') {
            formDefinition column: "formDefinition", sqlType: "text"
        }
    }

    static constraints = {
    }
}
