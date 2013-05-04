package com.nerderg.goodForm



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(FormVersion)
class FormVersionTests {

    void testConstraints() {
        FormDefinition formDefinition = new FormDefinition(name: 'test')
        FormVersion formVersion = new FormVersion(formVersionNumber: 1, formDefinitionDSL: '', formDefinition: formDefinition)
        mockForConstraintsTests(FormVersion, [formVersion])

        FormVersion notUnique = new FormVersion(formVersionNumber: 1, formDefinitionDSL: '', formDefinition: formDefinition)
        assert !notUnique.validate()

        assert 'unique' == notUnique.errors['formVersionNumber']

        FormVersion nullFields = new FormVersion()
        assert !nullFields.validate()
        assert 'nullable' == nullFields.errors['formDefinition']
        assert 'nullable' == nullFields.errors['formVersionNumber']
        assert 'nullable' == nullFields.errors['formDefinitionDSL']

        FormVersion allGood = new FormVersion(formVersionNumber: 2, formDefinitionDSL: '{}', formDefinition: formDefinition)
        assert allGood.validate()

    }

}
