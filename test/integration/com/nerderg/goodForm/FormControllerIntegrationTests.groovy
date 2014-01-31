package com.nerderg.goodForm

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

import static org.junit.Assert.assertEquals

import org.junit.After
import org.junit.Before
import org.junit.Test

class FormControllerIntegrationTests extends AbstractIntegrationTest {

    def rulesEngineService

    private FormController controller = new FormController()

    @Before
    void setUp() {
        super.setUp()
        JSONArray qset = JSON.parse('[\'Q1\']')

        controller.rulesEngineService = [ask: {String ruleSet, Map facts ->
            ['next': qset]
        }]

        JSONArray endqset = JSON.parse("['End']")
        formDataService.rulesEngineService = [ask: {String ruleSet, Map facts ->
            ['next': endqset, 'Q1': '']
        }]
    }

    @After
    void tearDown() {
        controller.rulesEngineService = rulesEngineService
    }

    @Test
    void testMandatoryField() {
        controller.createForm("SampleForm")
        assert controller.response.redirectedUrl.contains('continueForm')
        long id = controller.response.redirectedUrl.split('/').last().toLong()
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
        Map formData = controller.modelAndView.model.formData
        assert !formData.fieldErrors.isEmpty()
        assert formData.fieldErrors['Q1.names.givenNames0'] == 'Required field. Please fill in this field.'
        assert formData.fieldErrors['Q1.names.lastName0'] == 'Required field. Please fill in this field.'

    }

    @Test
    void testFormSubmission() {
        controller.createForm("SampleForm")
        long id = controller.response.redirectedUrl.split('/').last().toLong()
        controller.params.putAll(
                ['Q1.names.aliases.alias': '',
                 'Q1.names.lastName': 'Test',
                 'Q1.names.title': '',
                 'Q1.names.aliases.aliases.aliasType': '',
                 'Q1.names.givenNames': 'Test',
                 'Q1': ['names': ['lastName': '', 'title': '', 'givenNames': '',
                                  'aliases': ['alias': '', 'aliasType': '']]]
                ])
        controller.next(id)
        controller.endForm(id)
    }

    @Test
    void testNoFormName() {
        controller.createForm(null)
        assertEquals controller.message(code: "goodform.formName.supplied"), controller.flash.message
    }

    @Test
    void testInvalidFormName() {
        controller.createForm("Blah")
        assertEquals controller.message(code: "goodform.formName.invalid"), controller.flash.message
    }
}
