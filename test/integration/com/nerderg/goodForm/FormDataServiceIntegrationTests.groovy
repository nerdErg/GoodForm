package com.nerderg.goodForm

import org.junit.Test

class FormDataServiceIntegrationTests {
    def formDataService

    @Test
    void testFormCurrentDefinitionForName() {
        formDataService.forms.clear()

        String sampleForm = """ form {  //start with a 'form' element
                   question("Q1") {   //include a 'question' element with an identifier
                           "What is your name?" group: "names", {
                           "Title" text: 10, hint: "e.g. Mr, Mrs, Ms, Miss, Dr", suggest: "title", map: 'title'
                           "Given Names" text: 50, required: true, map: 'givenNames'
                           "Last or Family Name" text: 50, required: true, map: 'lastName'
                           "Date of life" date: "dd/MM/yyyy", required: true, map: 'dol'
                           "Have you been or are you known by any other names?" hint: "e.g. maiden name, previous married name, alias, name at birth", map: 'hasAlias', {
                               "List your other names" listOf: "aliases", {
                                   "Other name" text: 50, map: 'alias'
                                   "Type of name" text: 40, hint: "e.g maiden name", suggest: "nameType", map: 'aliasType'
                               }
                           }

                       }
                   }
                   question("Q2") {
                       "What is your favorite colour?" text: 20, map: 'faveColour', suggest: 'colour'
                   }

            }"""

        for (i in 0..99) {
            formDataService.createNewFormVersion('ContactDetails', sampleForm)
        }

        FormDefinition formDefinition = FormDefinition.findByName('ContactDetails')

        assert formDefinition.formVersions.size() == 100

        FormVersion formVersion

        formVersion = timeit('FormDefinition.currentVersion() took') {
            formDefinition.currentVersion()
        } as FormVersion

        assert formVersion.formVersionNumber == 100

    }

    private timeit(String msg, Closure c) {
        Long start = System.currentTimeMillis()
        def val = c()
        Long delta = System.currentTimeMillis() - start
        println "$msg $delta ms"
        return val
    }
}
