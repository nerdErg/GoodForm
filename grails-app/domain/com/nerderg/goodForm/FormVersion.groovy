package com.nerderg.goodForm

class FormVersion {

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
