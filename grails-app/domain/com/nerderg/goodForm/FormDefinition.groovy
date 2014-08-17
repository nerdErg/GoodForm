package com.nerderg.goodForm

/**
 * #domian, #persistance
 *
 * This domain class defines a particular form by name. It has many form versions associated with it which contain the
 * FormDefinitionDSL as a String.
 *
 * @see FormVersion
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

    public FormVersion currentVersion() {
        formVersions?.max{ FormVersion fv -> fv.formVersionNumber }
    }
}
