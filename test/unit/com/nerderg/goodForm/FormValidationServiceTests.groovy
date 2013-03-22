package com.nerderg.goodForm

import com.nerderg.goodForm.form.Form
import com.nerderg.goodForm.form.Question
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.junit.Before
import com.nerderg.goodForm.form.FormElement

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

    void testInvalidValue() {
        service.addCustomValidator("newValidator", {FormElement formElement, String fieldValue ->
            return true
        })
        form.question("Q1") {
            "New Validator" text: 50, required: true, validate: 'newValidator'
        }
        Question question = form["Q1"]
        def error = service.customValidation(question.formElement, 'abc')
        assertTrue("Error was not detected", error)
    }

    void testValidValue() {
        service.addCustomValidator("newValidator", {FormElement formElement, String fieldValue ->
            return false
        })
        form.question("Q1") {
            "New Validator" text: 50, required: true, validate: 'newValidator'
        }
        Question question = form["Q1"]
        def error = service.customValidation(question.formElement, 'abc')
        assertFalse("Error was not detected", error)
    }


}
