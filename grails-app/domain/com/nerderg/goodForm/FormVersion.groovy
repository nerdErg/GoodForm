package com.nerderg.goodForm
/**
 * #domian, #persistance
 *
 * Represents a version of a form definition.
 * This domain class contains the form definition and it's version number.
 *
 * @see FormDefinition
 * @author Peter McNeil
 */
class FormVersion {

    /**
     * The formDefinition DSL as a string
     */
    String formDefinitionDSL

    /**
     * Incrementing integer that represents the version number of the underlying form.
     */
    Integer formVersionNumber

    static belongsTo = [formDefinition: FormDefinition]

    static mapping = {
        formDefinitionDSL column: "formDefinition", sqlType: "text"
    }

    static constraints = {
        formVersionNumber unique: 'formDefinition'
    }
}
