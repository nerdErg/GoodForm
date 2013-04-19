package com.nerderg.goodForm

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin

import org.junit.Before

import com.nerderg.goodForm.form.Form
import com.nerderg.goodForm.form.Question

/**
 * @author Ross Rowe
 */
@TestFor(FormDataService)
@TestMixin(ControllerUnitTestMixin)
class FormDataServiceTests {

    FormInstance formInstance
    Form form
    FormValidationService formValidationService

    //TODO processNext

    @Before
    public void setUp() {
        service.goodFormService = new GoodFormService()
        formInstance = new FormInstance()
        form = new Form()
        formValidationService = new FormValidationService()
        service.addValidator(formValidationService.customValidation)
    }

    void testMandatoryFieldValidation() {

        form.question("Q1") {
            "Given Name" text: 50, required: true, map: 'givenName'
        }
        Question question = form.getAt("Q1")
        def error = service.validateAndProcessFields(question.formElement, [:], formInstance)
        assertTrue("Error was not detected", error)
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['givenName': 'Joe']], formInstance)
        assertFalse("Error was detected", error)
    }

    void testInvalidDateField() {
        form.question("Q1") {
            "Date Of Birth" date: "d/M/yyyy", map: 'dob'
        }
        Question question = form.getAt("Q1")
        def error = service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '2012/01/01']], formInstance)
        assertTrue("Error was not detected", error)
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '01/01/2012']], formInstance)
        assertFalse("Error was detected", error)
    }

    void testInvalidPatternField() {
        form.question("Q1") {
            "Given Name" text: 50, pattern: /[A-Za-z]+/, map: 'givenName'
        }
        Question question = form.getAt("Q1")

        def error = service.validateAndProcessFields(question.formElement, ['Q1': ['givenName': '1234']], formInstance)
        assertTrue("Error was not detected", error)
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['givenName': 'Joe']], formInstance)
        assertFalse("Error was not detected", error)
    }

    void testNumberToBigDecimal() {
        form.question("Q1") {
            "Age" number: 5, map: 'age'
        }
        Question question = form.getAt("Q1")
        Map formData = ['Q1': ['age': '23']]

        boolean error = service.validateAndProcessFields(question.formElement, formData, formInstance)
        assertFalse("Error was detected", error)
        assert formData.Q1.age instanceof BigDecimal
        assert formData.Q1.age == 23

        formData.Q1.age = '45.6'
        assert formData.Q1.age instanceof String
        error = service.validateAndProcessFields(question.formElement, formData, formInstance)
        assertFalse("Error was detected", error)
        assert formData.Q1.age instanceof BigDecimal
        assert formData.Q1.age == (45.6 as BigDecimal)

        formData.Q1.age = 'norman'
        assert formData.Q1.age instanceof String
        error = service.validateAndProcessFields(question.formElement, formData, formInstance)
        assertTrue("Error was not detected", error)
    }

    void testEach() {
        form.question("Q1") {
            "Rate these lollies" each: 'lolly', {
                "rate {lolly} out of ten" number: [0..10], map: 'rating'
            }
        }
        Question question = form.getAt("Q1")
        Map formData = [
                'lolly' : ['sherbet', 'gum', 'chocolate', 'carrot stick'],
                'Q1': [lolly: [sherbet: [rating: '4'], gum: [rating: '2'], chocolate: [rating: '9'], carrot_stick: [rating: '10']]]
        ]
        boolean error = service.validateAndProcessFields(question.formElement, formData, formInstance)
        assertFalse("Error was detected", error)
        assert formData.Q1.lolly.sherbet.rating instanceof BigDecimal
        assert formData.Q1.lolly.carrot_stick.rating instanceof BigDecimal

        formData.Q1.lolly.gum.rating = 'yucky'
        error = service.validateAndProcessFields(question.formElement, formData, formInstance)
        assertTrue("Error was not detected", error)

    }
}
