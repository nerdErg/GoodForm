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

    @Before public void setUp() {
        service.goodFormService = new GoodFormService()
    }

    void testMandatoryFieldValidation() {
        Form f = new Form()
        f.question("Q1") {
            "Given Name" text: 50, required: true
        }
        Question question = f.getAt("Q1")
        def formInstance = new FormInstance()
        def error = service.validateAndProcessFields(question.formElement, [:], formInstance)
        assertTrue("Error was not detected", error)
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['Given_Name':'Joe']], formInstance)
        assertFalse("Error was detected", error)
    }

    void testInvalidDateField() {
        fail "implement me"
    }

    void testInvalidPatternField() {
        fail "implement me"
    }
}
