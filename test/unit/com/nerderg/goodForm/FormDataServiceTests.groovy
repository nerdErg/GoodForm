package com.nerderg.goodForm

import com.nerderg.goodForm.form.Form
import com.nerderg.goodForm.form.Question
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.junit.Before

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

    @Before public void setUp() {
        service.goodFormService = new GoodFormService()
        formInstance = new FormInstance()
        form = new Form()
        formValidationService = new FormValidationService()
        service.addValidator(formValidationService.customValidation)
    }

    void testMandatoryFieldValidation() {

        form.question("Q1") {
            "Given Name" text: 50, required: true
        }
        Question question = form.getAt("Q1")
        def error = service.validateAndProcessFields(question.formElement, [:], formInstance)
        assertTrue("Error was not detected", error)
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['Given_Name': 'Joe']], formInstance)
        assertFalse("Error was detected", error)
    }

    void testInvalidDateField() {
        form.question("Q1") {
            "Date Of Birth" date: "d/M/yyyy"
        }
        Question question = form.getAt("Q1")
        def error = service.validateAndProcessFields(question.formElement, ['Q1': ['Date_Of_Birth': '2012/01/01']], formInstance)
        //TODO this actually parses into a date, but not the expected value - should the validator handle this somehow?
        assertFalse("Error was not detected", error)
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['Given_Name': '01/01/2012']], formInstance)
        assertFalse("Error was detected", error)
    }

    void testInvalidPatternField() {
        form.question("Q1") {
            "Given Name" text: 50, pattern: /[A-Za-z]+/
        }
        Question question = form.getAt("Q1")

        def error = service.validateAndProcessFields(question.formElement, ['Q1': ['Given_Name': '1234']], formInstance)
        assertTrue("Error was not detected", error)
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['Given_Name': 'Joe']], formInstance)
        assertFalse("Error was not detected", error)
    }

    void testValidPostcode() {
        def mockControl = mockFor(AddressWranglingService)
        mockControl.demand.isValidPostcode('2615') { String someItem -> true; }
        formValidationService.addressWranglingService = mockControl.createMock()

        form.question("Q1") {
            "Postcode" text: 50, required: true, validate: 'postcode'
        }
        Question question = form.getAt("Q1")
        def error = service.validateAndProcessFields(question.formElement, ['Q1': ['Postcode': '2615']], formInstance)
        assertFalse("Error was not detected", error)
    }

    void testPhoneNumber() {
        form.question("Q1") {
            "Phone Number" phone: 50, required: true, map: 'phone'
        }
        Question question = form.getAt("Q1")
        //digit only
        def error = service.validateAndProcessFields(question.formElement, ['Q1': ['Phone_Number': 'abc']], formInstance)
        assertTrue("Error was not detected", error)
        //min length
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['Phone_Number': '12345']], formInstance)
        assertTrue("Error was not detected", error)
        //number format
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['Phone_Number': '1234567890']], formInstance)
        assertTrue("Error was not detected", error)
        //valid phone
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['Phone_Number': '0412345678']], formInstance)
        assertFalse("Error was not detected", error)
    }
}
