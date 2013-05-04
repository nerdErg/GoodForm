package com.nerderg.goodForm

import org.junit.Before

/**
 * @author Ross Rowe
 */
abstract class AbstractIntegrationTest {

    def formDataService

    @Before
    void setUp() {
        formDataService.forms.clear()
        // create form definition
        String sampleForm = """
                   form {
                       question("Q1") {   //include a 'question' element with an identifier
                          "What is your name?" group: "names", {
                             "Title" text: 10, hint: "e.g. Mr, Mrs, Ms, Miss, Dr", suggest: "title", map: 'title'
                             "Given Names" text: 50, required: true, map: 'givenNames'
                             "Last or Family Name" text: 50, required: true, map: 'lastName'
                             "Have you been or are you known by any other names?" hint: "e.g. maiden name, previous married name, alias, name at birth", map: 'hasAlias', {
                                 "List your other names" listOf: "aliases", {
                                     "Other name" text: 50, map: 'alias'
                                     "Type of name" text: 40, hint: "e.g maiden name", suggest: "nameType", map: 'aliasType'
                                 }
                             }
                         }
                      }
                   }"""

        formDataService.createNewFormVersion('SampleForm', sampleForm)
    }
}
