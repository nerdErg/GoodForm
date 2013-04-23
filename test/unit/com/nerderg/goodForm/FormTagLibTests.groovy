package com.nerderg.goodForm

import com.nerderg.goodForm.form.FormElement
import com.nerderg.taglib.NerdergFormtagsTagLib
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.services.ServiceUnitTestMixin
import org.junit.Before

@TestFor(FormTagLib)
@TestMixin(ServiceUnitTestMixin)
class FormTagLibTests {

    FormElement formElement

    @Before
    void setUp() {

        tagLib.goodFormService = new GoodFormService()
        mockTagLib(NerdergFormtagsTagLib.class)
    }

    void testPick() {
        formElement = new FormElement()
        formElement.form("Dummy element", [pick: 1, map: 'test'], null) {
            "are you male?" default: true
        }
        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [:]]).trim()

        assert result.contains('input type=\'radio\'')

    }

    void testHeader() {
        formElement = new FormElement()
        formElement.form("This is a test", [heading: 1, map: 'test'], null) {
            "A Test Heading"
        }
        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [:]]).trim()
        assert result.contains('<h1>This is a test</h1>')
    }

    void testNumber() {
        formElement = new FormElement()
        formElement.form("This is a test", [number: 5, map: 'test'], null) {
            "A Test Heading"
        }
        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [:]]).trim()
        assert result.contains('input type=\'number\'')
    }
}
