package com.nerderg.goodForm

import com.nerderg.goodForm.form.Form
import com.nerderg.goodForm.form.Question
import grails.test.mixin.TestFor
import org.junit.Before
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(FormValidationService)
@TestMixin(ControllerUnitTestMixin)
class FormValidationServiceTests {

    Form form

    @Before public void setUp() {
        form = new Form()
    }

    void testPhoneNumber() {
        form.question("Q1") {
            "Phone Number" text: 50, required: true, validate: 'phone'
        }
        Question question = form.getAt("Q1")
        //digit only
        def error = service.customValidation(question.formElement, 'abc')
        assertTrue("Error was not detected", error)
        //min length
        error = service.customValidation(question.formElement, '12345')
        assertTrue("Error was not detected", error)
        //number format
        error = service.customValidation(question.formElement, '1234567890')
        assertTrue("Error was not detected", error)
        //valid phone
        error = service.customValidation(question.formElement, '0412345678')
        assertFalse("Error was not detected", error)
    }

    void testInvalidPostcode() {
        def mockControl = mockFor(AddressWranglingService)
        mockControl.demand.isValidPostcode('2615') { String someItem -> false; }
        service.addressWranglingService = mockControl.createMock()
        form.question("Q1") {
            "Postcode" text: 4, required: true, validate: 'postcode'
        }
        Question question = form.getAt("Q1")
        def error = service.customValidation(question.formElement, '2615')
        assertTrue("Error was detected", error)
    }

    void testValidPostcode() {
        def mockControl = mockFor(AddressWranglingService)
        mockControl.demand.isValidPostcode('2615') { String someItem -> true; }
        service.addressWranglingService = mockControl.createMock()
        form.question("Q1") {
            "Postcode" text: 4, required: true, validate: 'postcode'
        }
        Question question = form.getAt("Q1")
        def error = service.customValidation(question.formElement, '2615')
        assertFalse("Error was detected", error)
    }


}
