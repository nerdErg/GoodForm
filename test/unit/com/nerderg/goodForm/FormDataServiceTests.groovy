package com.nerderg.goodForm

import com.nerderg.goodForm.form.Form
import com.nerderg.goodForm.form.Question
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.junit.Before
import org.junit.Test
import org.springframework.context.MessageSource

/**
 * @author Ross Rowe
 * @author Peter McNeil
 */
@TestFor(FormDataService)
@TestMixin(DomainClassUnitTestMixin)
class FormDataServiceTests {

    FormInstance formInstance
    Form form
    FormValidationService formValidationService

    //TODO processNext

    @Before
    public void setUp() {
        service.goodFormService = new GoodFormService()
        formValidationService = new FormValidationService()
        service.formValidationService = formValidationService
        service.addValidator(formValidationService.customValidation)
        formInstance = new FormInstance()
        form = new Form()

        def msgSrc = mockFor(MessageSource.class, true)
        //not sure I care how many times this gets called
        msgSrc.demand.getMessage(0..8) { String code, Object[] args, String defaultMessage, Locale locale ->
            return defaultMessage
        }
        formValidationService.messageSource = msgSrc.createMock()
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

    @Test
    void listDateValidation() {
        form.question("Q1") {
            "What is your birthday?" listOf: "birthdays", {
                "Birthday" date: 'dd/MM/yyyy', map: 'dob'
            }
        }
        Question question = form.getAt("Q1")
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['birthdays': ['dob': ['2012/01/01', '01/01/2012']]]], formInstance)
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['birthdays': ['dob': ['01/01/2012', '01/01/2012']]]], formInstance)
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['birthdays': ['dob': (String[])['2012/01/01', '01/01/2012']]]], formInstance)
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['birthdays': ['dob': (String[])['01/01/2012', '01/01/2012']]]], formInstance)
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

        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': 23]], formInstance)

    }

    void testMaxDateField() {
        form.question("Q1") {
            "Date Of Birth" date: "d/M/yyyy", map: 'dob', max: '01/01/2000'
        }
        Question question = form.getAt("Q1")
        def error = service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '02/01/2000']], formInstance)
        assertTrue("Error was not detected", error)
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
        assert question.formElement.attr.error == 'goodform.validate.number.isnt'

        formData.Q1.age = ['23', 'norman'] as String[]
        assert formData.Q1.age instanceof String[]
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert question.formElement.attr.error == 'goodform.validate.number.isnt'

    }

    private void numberMaxMinCommon(question, formData) {
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.Q1.age instanceof BigDecimal
        assert formData.Q1.age == 23

        formData.Q1.age = '0'
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)

        formData.Q1.age = '10'
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)

        formData.Q1.age = ['10', '22'] as String[]
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

        formData.Q1.age = ['10', '24'] as String[]
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert question.formElement.attr.error == 'goodform.validate.number.tobig'

    }

    void testNumberMaxMin() {
        form.question("Q1") {
            "Age" number: 5, map: 'age', max: 23, min: 0
        }
        Question question = form.getAt("Q1")
        Map formData = ['Q1': ['age': '23']]
        numberMaxMinCommon(question, formData)
    }

    void testNumberRange() {
        form.question("Q1") {
            "Age" number: 0..23, map: 'age'
        }
        Question question = form.getAt("Q1")
        Map formData = ['Q1': ['age': '23']]
        numberMaxMinCommon(question, formData)
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

    void testCreateNewFormVersion() {
        String sampleForm = """ form {  //start with a 'form' element
                   question("Q1") {   //include a 'question' element with an identifier
                           "What is your name?" group: "names", {
                           "Title" text: 10, hint: "e.g. Mr, Mrs, Ms, Miss, Dr", suggest: "title", map: 'title'
                           "Given Names" text: 50, required: true, map: 'givenNames'
                           "Last or Family Name" text: 50, required: true, map: 'lastName'
                           "Date of life" date: "dd/MM/yyyy", required: true, map: 'dol'
                           "Have you been or are you known by any other names?" hint: "e.g. maiden name, previous married name, alias, name at birth", map: 'hasAlias', {
                               "List your other names" listOf: "aliases", {
                                   "Other name" text: 50, map: 'alias'
                                   "Type of name" text: 40, hint: "e.g maiden name", suggest: "nameType", map: 'aliasType'
                               }
                           }

                       }
                   }
                   question("Q2") {
                       "What is your favorite colour?" text: 20, map: 'faveColour', suggest: 'colour'
                   }

            }"""
        mockDomain(FormDefinition)
        mockDomain(FormVersion)
        FormVersion formVersion = service.createNewFormVersion('ContactDetails', sampleForm)
        assert formVersion.formVersionNumber == 1
        assert formVersion.formDefinitionDSL == sampleForm
        assert formVersion.formDefinition != null
        assert formVersion.formDefinition instanceof FormDefinition
        assert formVersion.formDefinition.name == 'ContactDetails'
        assert formVersion.formDefinition.formVersions.size() == 1
        assert formVersion.formDefinition.formVersions.contains(formVersion)

        FormVersion formVersion2 = service.createNewFormVersion('ContactDetails', sampleForm)
        assert formVersion.formVersionNumber == 1
        assert formVersion.formDefinitionDSL == sampleForm
        assert formVersion.formDefinition != null
        assert formVersion2.formVersionNumber == 2
        assert formVersion.formDefinitionDSL == sampleForm
        assert formVersion2.formDefinition == formVersion.formDefinition
        assert formVersion.formDefinition.formVersions.size() == 2
        assert formVersion.formDefinition.formVersions.contains(formVersion)
        assert formVersion.formDefinition.formVersions.contains(formVersion2)

    }
}
