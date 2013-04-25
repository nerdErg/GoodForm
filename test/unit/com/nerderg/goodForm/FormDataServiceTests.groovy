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
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '2012/01/01']], formInstance)
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '01/01/-23']], formInstance)
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': 'fred']], formInstance)

        //dates shouldn't roll over, so test for it (java date trap)
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '34/2/2012']], formInstance)
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '30/13/2012']], formInstance)
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '0/10/2012']], formInstance)

        //test for some valid dates

        //this is a valid date for the year 12
        /* @see http://docs.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html.
        For parsing, if the number of pattern letters is more than 2, the year is interpreted literally, regardless of
        the number of digits. So using the pattern "MM/dd/yyyy", "01/11/12" parses to Jan 11, 12 A.D. */
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '01/01/12']], formInstance)
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '01/01/12345']], formInstance)

        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '01/01/2012']], formInstance)
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '1/1/2012']], formInstance)
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '31/12/1865']], formInstance)
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '31/1/3999']], formInstance)

        //leap years
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '29/02/2000']], formInstance)
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '29/02/2001']], formInstance)

    }

    void testMaxDateField() {
        form.question("Q1") {
            "Date Of Birth" date: "d/M/yyyy", map: 'dob', max: '01/01/2000'
        }
        Question question = form.getAt("Q1")
        def error = service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '02/01/2000']], formInstance)
        assertTrue("Error was not detected", error)
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '01/01/2000']], formInstance)
        assertFalse("Error was detected", error)
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '31/12/1999']], formInstance)
        assertFalse("Error was detected", error)
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '01/01/2000']], formInstance)
        assertFalse("Error was detected", error)
    }

    void testMinDateField() {
        form.question("Q1") {
            "Date Of Birth" date: "d/M/yyyy", map: 'dob', min: '1/1/2000'
        }
        Question question = form.getAt("Q1")
        def error = service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '31/12/1999']], formInstance)
        assertTrue("Error was not detected", error)
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '02/01/2000']], formInstance)
        assertFalse("Error was detected", error)
        error = service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '01/01/2000']], formInstance)
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

        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.Q1.age instanceof BigDecimal
        assert formData.Q1.age == 23

        formData.Q1.age = '45.6'
        assert formData.Q1.age instanceof String
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.Q1.age instanceof BigDecimal
        assert formData.Q1.age == (45.6 as BigDecimal)

        formData.Q1.age = 'norman'
        assert formData.Q1.age instanceof String
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
    }

    void testNumberMaxMin(){
        form.question("Q1") {
            "Age" number: 5, map: 'age', max: 23, min: 0
        }
        Question question = form.getAt("Q1")
        Map formData = ['Q1': ['age': '23']]
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.Q1.age instanceof BigDecimal
        assert formData.Q1.age == 23

        formData.Q1.age = '0'
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)

        formData.Q1.age = '10'
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)

        formData.Q1.age = '45.6'
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert question.formElement.attr.error == 'goodform.validate.number.tobig'

        formData.Q1.age = '23.0000001'
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert question.formElement.attr.error == 'goodform.validate.number.tobig'

        formData.Q1.age = '-0.6'
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert question.formElement.attr.error == 'goodform.validate.number.tosmall'
    }

    void testNumberRange(){
        form.question("Q1") {
            "Age" number: 0..23, map: 'age'
        }
        Question question = form.getAt("Q1")
        Map formData = ['Q1': ['age': '23']]
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.Q1.age instanceof BigDecimal
        assert formData.Q1.age == 23

        formData.Q1.age = '0'
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)

        formData.Q1.age = '10'
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)

        formData.Q1.age = '45.6'
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert question.formElement.attr.error == 'goodform.validate.number.tobig'

        formData.Q1.age = '23.0000001'
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert question.formElement.attr.error == 'goodform.validate.number.tobig'

        formData.Q1.age = '-0.6'
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert question.formElement.attr.error == 'goodform.validate.number.tosmall'
    }

    void testEach() {
        form.question("Q1") {
            "Rate these lollies" each: 'lolly', {
                "rate {lolly} out of ten" number: [0..10], map: 'rating'
            }
        }
        Question question = form.getAt("Q1")
        Map formData = [
                'lolly': ['sherbet', 'gum', 'chocolate', 'carrot stick'],
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
