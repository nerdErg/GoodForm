package com.nerderg.goodForm

import grails.test.mixin.TestFor
import net.sf.json.JSONArray
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.junit.After
import org.junit.Before
import org.junit.Test

@TestFor(FormController)
class FormControllerIntegrationTests {

    def rulesEngineService

    def formDataService

    def g = new ValidationTagLib()

    @Before
    void setUp() {
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

        if (!FormDefinition.get(1)) {
            FormDefinition formDefinition = new FormDefinition(name: 'SampleForm', formDefinition: sampleForm, formVersion: 1)
            formDefinition.save()
        }
        controller.rulesEngineService = [ask: {String ruleSet, Map facts ->
            ['next': JSONArray.fromObject('[\'Q1\']')]
        }]
        formDataService.rulesEngineService = [ask: {String ruleSet, Map facts ->
            ['next': JSONArray.fromObject('[\'End\']'), 'Q1': JSONArray.fromObject('[\'End\']')]
        }]
    }

    @After
    void tearDown() {
        controller.rulesEngineService = rulesEngineService
    }

    @Test
    void testMandatoryField() {
        controller.createForm("SampleForm")
        def id = controller.modelAndView.model.formInstance.id
        //todo is there an easier way to simulate the form parameters?
        controller.params.putAll(
                ['Q1.names.aliases.alias': '',
                        'Q1.names.lastName': '',
                        'Q1.names.title': '',
                        'Q1.names.aliases.aliases.aliasType': '',
                        'Q1.names.givenNames': '',
                        'Q1': ['names': ['lastName': '', 'title': '', 'givenNames': '',
                                'aliases': ['alias': '', 'aliasType': '']]]
                ])
        controller.next(id)
        controller.modelAndView.model.form
        //TODO validate that mandatory field errors are displayed
    }

    @Test
    void testNoFormName() {
        controller.createForm(null)
        org.junit.Assert.assertEquals g.message(code: "goodform.formName.supplied"), flash.message
    }

    @Test
    void testInvalidFormName() {
        controller.createForm("Blah")
        org.junit.Assert.assertEquals g.message(code: "goodform.formName.invalid"), flash.message
    }
}
