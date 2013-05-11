package com.nerderg.goodForm

import com.nerderg.goodForm.form.Form
import com.nerderg.goodForm.form.Question
import grails.converters.JSON
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONElement
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
        question.formElement.attr.name = 'Q1.givenName'
        assert service.validateAndProcessFields(question.formElement,[fieldErrors: [:]], formInstance)
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['givenName': 'Joe'], fieldErrors: [:]], formInstance)
    }

    @Test
    void listDateValidation() {
        form.question("Q1") {
            "What is your birthday?" listOf: "birthdays", {
                "Birthday" date: 'dd/MM/yyyy', map: 'dob'
            }
        }
        Question question = form.getAt("Q1")
        question.formElement.attr.name = 'Q1.birthdays'
        question.formElement.subElements[0].attr.name = 'Q1.birthdays.dob'
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['birthdays': ['dob': ['2012/01/01', '01/01/2012']]], fieldErrors: [:]], formInstance)
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['birthdays': ['dob': ['01/01/2012', '01/01/2012']]], fieldErrors: [:]], formInstance)
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['birthdays': ['dob': (String[])['2012/01/01', '01/01/2012']]], fieldErrors: [:]], formInstance)
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['birthdays': ['dob': (String[])['01/01/2012', '01/01/2012']]], fieldErrors: [:]], formInstance)
    }


    void testInvalidDateField() {
        form.question("Q1") {
            "Date Of Birth" date: "d/M/yyyy", map: 'dob'
        }
        Question question = form.getAt("Q1")
        question.formElement.attr.name = 'Q1.dob'

        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '2012/01/01'], fieldErrors: [:]], formInstance)
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '01/01/-23'], fieldErrors: [:]], formInstance)
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': 'fred'], fieldErrors: [:]], formInstance)

        //dates shouldn't roll over, so test for it (java date trap)
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '34/2/2012'], fieldErrors: [:]], formInstance)
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '30/13/2012'], fieldErrors: [:]], formInstance)
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '0/10/2012'], fieldErrors: [:]], formInstance)

        //test for some valid dates

        //this is a valid date for the year 12
        /* @see http://docs.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html.
        For parsing, if the number of pattern letters is more than 2, the year is interpreted literally, regardless of
        the number of digits. So using the pattern "MM/dd/yyyy", "01/11/12" parses to Jan 11, 12 A.D. */
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '01/01/12'], fieldErrors: [:]], formInstance)
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '01/01/12345'], fieldErrors: [:]], formInstance)

        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '01/01/2012'], fieldErrors: [:]], formInstance)
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '1/1/2012'], fieldErrors: [:]], formInstance)
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '31/12/1865'], fieldErrors: [:]], formInstance)
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '31/1/3999'], fieldErrors: [:]], formInstance)

        //leap years
        assert !service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '29/02/2000'], fieldErrors: [:]], formInstance)
        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': '29/02/2001'], fieldErrors: [:]], formInstance)

        assert service.validateAndProcessFields(question.formElement, ['Q1': ['dob': 23], fieldErrors: [:]], formInstance)

    }

    void testMaxDateField() {
        form.question("Q1") {
            "Date Of Birth" date: "d/M/yyyy", map: 'dob', max: '01/01/2000'
        }
        Question question = form.getAt("Q1")
        question.formElement.attr.name = 'Q1.dob'

        Map formData = ['Q1': ['dob': '02/01/2000'], fieldErrors: [:]]
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.dob'] == 'goodform.validate.date.greaterThan'

        formData = ['Q1': ['dob': '31/12/1999'], fieldErrors: [:]]
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.dob'] == null

        formData = ['Q1': ['dob': '01/01/2000'], fieldErrors: [:]]
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.dob'] == null
    }

    void testMinDateField() {
        form.question("Q1") {
            "Date Of Birth" date: "d/M/yyyy", map: 'dob', min: '1/1/2000'
        }
        Question question = form.getAt("Q1")
        question.formElement.attr.name = 'Q1.dob'

        Map formData = ['Q1': ['dob': '31/12/1999'], fieldErrors: [:]]
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.dob'] == 'goodform.validate.date.lessThan'

        formData = ['Q1': ['dob': '02/01/2000'], fieldErrors: [:]]
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.dob'] == null

        formData = ['Q1': ['dob': '01/01/2000'], fieldErrors: [:]]
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.dob'] == null
    }

    void testInvalidPatternField() {
        form.question("Q1") {
            "Given Name" text: 50, pattern: /[A-Za-z]+/, map: 'givenName'
        }
        Question question = form.getAt("Q1")
        question.formElement.attr.name = 'Q1.givenName'

        Map formData = ['Q1': ['givenName': '1234'], fieldErrors: [:]]
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.givenName'] == 'goodform.validate.invalid.pattern'

        formData = ['Q1': ['givenName': 'Joe'], fieldErrors: [:]]
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.givenName'] == null
    }

    void testInvalidPatternFieldWithMessage() {
        form.question("Q1") {
            "Given Name" text: 50, pattern: [/[A-Za-z]+/, 'name can only have chars'], map: 'givenName'
        }
        Question question = form.getAt("Q1")
        question.formElement.attr.name = 'Q1.givenName'

        Map formData = ['Q1': ['givenName': '1234'], fieldErrors: [:]]
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.givenName'] == 'name can only have chars'

        formData = ['Q1': ['givenName': 'Joe'], fieldErrors: [:]]
        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.givenName'] == null
    }

    void testNumberToBigDecimal() {
        form.question("Q1") {
            "Age" number: 5, map: 'age'
        }
        Question question = form.getAt("Q1")
        question.formElement.attr.name = 'Q1.age'
        Map formData = ['Q1': ['age': '23'], fieldErrors: [:]]

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
        assert formData.fieldErrors['Q1.age'] == 'goodform.validate.number.isnt'

        formData.Q1.age = ['23', 'norman'] as String[]
        assert formData.Q1.age instanceof String[]
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.age1'] == 'goodform.validate.number.isnt'

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
        formData.fieldErrors = [:]
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.age'] == 'goodform.validate.number.tobig'

        formData.Q1.age = '23.0000001'
        formData.fieldErrors = [:]
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.age'] == 'goodform.validate.number.tobig'

        formData.Q1.age = '-0.6'
        formData.fieldErrors = [:]
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.age'] == 'goodform.validate.number.tosmall'

        formData.Q1.age = ['10', '24'] as String[]
        formData.fieldErrors = [:]
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)
        assert formData.fieldErrors['Q1.age1'] == 'goodform.validate.number.tobig'

    }

    void testNumberMaxMin() {
        form.question("Q1") {
            "Age" number: 5, map: 'age', max: 23, min: 0
        }
        Question question = form.getAt("Q1")
        question.formElement.attr.name = 'Q1.age'
        Map formData = ['Q1': ['age': '23'],fieldErrors: [:]]
        numberMaxMinCommon(question, formData)
    }

    void testNumberRange() {
        form.question("Q1") {
            "Age" number: 0..23, map: 'age'
        }
        Question question = form.getAt("Q1")
        question.formElement.attr.name = 'Q1.age'
        Map formData = ['Q1': ['age': '23'],fieldErrors: [:]]
        numberMaxMinCommon(question, formData)
    }

    void testEach() {
        form.question("Q1") {
            "Rate these lollies" each: 'lolly', {
                "rate {lolly} out of ten" number: 0..10, map: 'rating'
            }
        }
        Question question = form.getAt("Q1")
        question.formElement.attr.name = 'Q1.lolly'
        question.formElement.subElements[0].attr.name = 'Q1.lolly.rating'

        Map formData = [
                'lolly': ['sherbet', 'gum', 'chocolate', 'carrot stick'],
                'Q1': [lolly: [sherbet: [rating: '4'], gum: [rating: '2'], chocolate: [rating: '9'], carrot_stick: [rating: '10']]],
                fieldErrors: [:]
        ]

        assert !service.validateAndProcessFields(question.formElement, formData, formInstance)
        println formData.Q1.lolly.sherbet.rating.class
        assert formData.Q1.lolly.sherbet.rating instanceof BigDecimal
        assert formData.Q1.lolly.carrot_stick.rating instanceof BigDecimal

        formData.Q1.lolly.gum.rating = 'yucky'
        assert service.validateAndProcessFields(question.formElement, formData, formInstance)

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

    void testIsCollectionOrArray(){

        String subst = 'boo'
        assert !("this is a ${subst} gstring" instanceof Object[])
        assert !("this is a ${subst} gstring" instanceof Collection)
        assert !("this is a ${subst} gstring" instanceof List)
        assert !('this is a string' instanceof Object[])
        assert !('this is a string' instanceof Collection)
        assert !('this is a string' instanceof List)

        assert !service.isCollectionOrArray('this is a string')
        assert !service.isCollectionOrArray("this is a ${['list in a string','blah']} gstring")
        ArrayList<String> listOfString = ['this is a string in a list']
        assert listOfString instanceof ArrayList
        assert service.isCollectionOrArray(listOfString)
        String[] stringArray = ['this is a string in an Array','another string']
        assert stringArray instanceof String[]
        assert service.isCollectionOrArray(stringArray)

        Set set = ['string in a set','another string']
        assert set instanceof HashSet
        assert service.isCollectionOrArray(set)

        //maps should not match
        LinkedHashMap aHashMap = [fred: 'smith']
        assert aHashMap instanceof LinkedHashMap
        assert !service.isCollectionOrArray(aHashMap)

        String jsonString = """{
   "Job1": {
      "order": "0",
      "names": {
         "lastName": "McNeil",
         "givenNames": "Peter",
         "hasAlias": {
            "yes": "on",
            "aliases": {
               "alias": [
                  "John Smith",
                  "wally donucker"
               ],
               "aliasType": [
                  "pseudonym",
                  "pseudonym"
               ]
            }
         },
         "Title": "Mr"
      }
   }
}
"""

        def formData = JSON.parse(jsonString)
        assert formData instanceof JSONElement
        assert formData.Job1.names.hasAlias.aliases.alias instanceof JSONArray
        assert service.isCollectionOrArray(formData.Job1.names.hasAlias.aliases.alias)
        assert formData.Job1.names.lastName instanceof String
        assert !service.isCollectionOrArray(formData.Job1.names.lastName)

    }
}
