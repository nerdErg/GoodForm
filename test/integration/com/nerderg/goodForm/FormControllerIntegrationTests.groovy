package com.nerderg.goodForm

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

import org.junit.After
import org.junit.Before
import org.junit.Test

import com.nerderg.goodForm.form.Form
import com.nerderg.goodForm.form.FormElement

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
        Form form = controller.modelAndView.model.form
        boolean foundError = false
        form.getQuestions().each {
            it.formElement.subElements.each { FormElement sub ->
                foundError = sub.attr.error || foundError
            }
        }
        assertTrue("Error was not found", foundError)
    }

    @Test
    void testFormSubmission() {
        controller.createForm("SampleForm")
        def id = controller.modelAndView.model.formInstance.id
        //todo is there an easier way to simulate the form parameters?
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
