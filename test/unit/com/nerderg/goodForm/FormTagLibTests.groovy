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

    def goodFormService

    @Before
    void setUp() {

        tagLib.goodFormService = new GoodFormService()
        mockTagLib(NerdergFormtagsTagLib.class)
    }

    void testPick() {
        formElement = new FormElement()
        formElement.form("This is a test", [pick: 1, map: 'test'], null) {
            "are you male?" default: true
            "female"()
            "other"() {
                "well what the heck are you?" text: 80, map: 'other'
            }
        }
        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [:]]).trim()

    }
}
