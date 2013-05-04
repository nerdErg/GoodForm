package com.nerderg.goodForm

/**
 * Handles persisting the form definition in the database.
 *
 * @author Peter McNeil
 */
class FormDefinition {

    /**
     * The name of the form.  Must be unique.
     */
    String name

    static hasMany = [formVersions: FormVersion]

    static constraints = {
       name unique: true
    }
}
