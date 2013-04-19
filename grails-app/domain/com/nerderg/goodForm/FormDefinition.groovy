package com.nerderg.goodForm

/**
 * Handles persisting the form definition in the database.
 *
 * @author Peter McNeil
 */
class FormDefinition {

    String formDefinition
    /**
     * The name of the form.  Must be unique.
     */
    String name

    /**
     * Incrementing integer that represents the version number of the underlying form.
     */
    Integer formVersion

    static mapping = {
        formDefinition column: "formDefinition", sqlType: "text"
    }

    static constraints = {
       name unique: 'formVersion'
    }
}
