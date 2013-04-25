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
        formElement = new FormElement()
        tagLib.goodFormService = new GoodFormService()
        tagLib.formDataService = new FormDataService()
        mockTagLib(NerdergFormtagsTagLib.class)
    }

    void testPick() {
        formElement.form("Dummy element", [pick: 1, map: 'test'], null) {
            "are you male?" default: true
        }
        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [:]]).trim()

        assert result.contains('input type=\'radio\'')

    }

    void testHeader() {
        formElement.form("This is a test", [heading: 1, map: 'test'], null) {
            "A Test Heading"
        }
        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [:]]).trim()
        assert result.contains('<h1>This is a test</h1>')
    }

    void testNumber() {
        formElement.form("This is a test", [number: 5, map: 'test'], null) { }
        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [:]]).trim()
        assert result == """<div class='prop'>
<span class='name'><label for='.test'>This is a test:</label></span>
<span class='value' title='This is a test'><input type='number' name='.test' value='' id='.test' size='5' maxlength='5'/><span class='units'></span><span class='required'></span><span class='hint'></span></span>
</div>"""
    }

    void testNumberRange() {
        formElement.form("This is a test", [number: 0..21, map: 'test'], null) { }
        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [:]]).trim()
        println result
        assert result == """<div class='prop'>
<span class='name'><label for='.test'>This is a test:</label></span>
<span class='value' title='This is a test'><input type='number' name='.test' value='' id='.test' size='2' maxlength='2' max='21' min='0'/><span class='units'></span><span class='required'></span><span class='hint'></span></span>
</div>"""
    }

    void testPhone() {
        formElement.form("This is a test", [phone: 15, map: 'test'], null) {
            "+61400123456"
        }
        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [:]]).trim()
        assert result.contains('input type=\'tel\'')
    }

    void testMoney() {
        formElement.form("This is a test", [money: 5, map: 'test'], null) {
            "123.00"
        }
        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [:]]).trim()
        assert result.contains('class=\'money\'')
    }

    void testDate() {
        formElement.form("This is a test", [date: "d/M/yyyy", map: 'test'], null) {
            "01/01/2013"
        }
        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [:]]).trim()
        assert result.contains('class=\'date\'')
    }

    void testDatetime() {
        formElement.form("This is a test", [datetime: "d/M/yyyy", map: 'test'], null) {
            "01/01/2013"
        }
        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [:]]).trim()
        assert result.contains('class=\'date\'')
    }
}
