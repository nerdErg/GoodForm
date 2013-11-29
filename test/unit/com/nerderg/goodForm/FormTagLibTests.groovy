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

    void testGroup() {
        FormElement subElement = new FormElement()
        subElement.form("Dummy element", [text: 10, map: 'test'], null) {
            "testing"
        }
        formElement.form("Dummy element", [group: 'test', map: 'test'], null) {
            "testing"
        }
        formElement.subElements.add(subElement)
        formElement.attr.name = 'test'
        subElement.attr.name = 'test.test'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<h2>Dummy element <span class='hint'></span></h2><div class='questionGroup'><div class='prop'>
<span class='name'><label for='test.test'>Dummy element:</label></span>
<span class='value' title='Dummy element'><input type='text' name='test.test' value='' id='test.test' size='10' maxlength='10'/><span class='required'></span><span class='hint'></span></span>
</div>
</div>"""
    }

    void testListOf() {
        FormElement subElement = new FormElement()
        subElement.form("Dummy element", [text: 10, map: 'test'], null) {
            "testing"
        }
        formElement.form("Dummy element", [listOf: 'test', map: 'test'], null) {
            "testing"
        }
        formElement.subElements.add(subElement)
        formElement.attr.name = 'test'
        subElement.attr.name = 'test.test'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<h2>Dummy element <span class='hint'></span></h2><div class='listContainer'><div class='questionListOfItem'><div class='prop'>
<span class='name'><label for='test.test'>Dummy element:</label></span>
<span class='value' title='Dummy element'><input type='text' name='test.test' value='' id='test.test' size='10' maxlength='10'/><span class='required'></span><span class='hint'></span></span>
</div>
<div class='removeForm'><img src='/images/icons/delete.png' title='Remove this' alt='+'/><span class='hint'>goodform.item.remove</span></div></div></div><div class='addAnotherForm'><img src='/images/icons/add.png' title='add another' alt='+'/><span class='hint'>goodform.item.addAnother</span></div>"""
    }

    void testText() {
        formElement.form("Dummy element", [text: 10, map: 'test'], null) {
            "testing"
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class='prop'>
<span class='name'><label for='test'>Dummy element:</label></span>
<span class='value' title='Dummy element'><input type='text' name='test' value='' id='test' size='10' maxlength='10'/><span class='required'></span><span class='hint'></span></span>
</div>"""
    }

    void testLargeText() {
        formElement.form("Dummy element", [text: 200, map: 'test'], null) {
            "testing"
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class='prop'>
<span class='name'><label for='test'>Dummy element:</label></span>
<span class='value' title='Dummy element'><textarea name='test' id='test'  cols='80' rows='3'></textarea>
                <span class='required'></span><span class='hint'></span>
                </span>
</div>"""
    }

    void testPick() {
        formElement.form("Dummy element", [pick: 1, map: 'test'], null) {
            "are you male?" default: true
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()

        assert result.contains('input type=\'radio\'')
        assert result == """<div class='prop'><span class='name'>Dummy element<span class='required'></span></span><div class='questionPick'><div class='prop'>
<span class='name'><label>are you male?:</label></span>
<span class='value' title='are you male?'><input type='radio' name='test' id='null' value='are you male?' />
                    <span class='required'></span><span class='hint'></span>
                    </span>
</div>
</div></div>"""
    }

    void testHeader() {
        formElement.form("This is a test", [heading: 1, map: 'test'], null) {
            "A Test Heading"
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == '<h1>This is a test</h1>'
    }

    void testNumber() {
        formElement.form("This is a test", [number: 5, map: 'test'], null) {}
        formElement.attr.name = 'test'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class='prop'>
<span class='name'><label for='test'>This is a test:</label></span>
<span class='value' title='This is a test'><input type='number' name='test' value='' id='test' size='5' maxlength='5'/><span class='units'></span><span class='required'></span><span class='hint'></span></span>
</div>"""
    }

    void testNumberRange() {
        formElement.form("This is a test", [number: 0..21, map: 'test'], null) {}
        formElement.attr.name = 'test'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class='prop'>
<span class='name'><label for='test'>This is a test:</label></span>
<span class='value' title='This is a test'><input type='number' name='test' value='' id='test' size='2' maxlength='2' max='21' min='0'/><span class='units'></span><span class='required'></span><span class='hint'></span></span>
</div>"""
    }

    void testPhone() {
        formElement.form("This is a test", [phone: 15, map: 'test'], null) {
            "+61400123456"
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result.contains('input type=\'tel\'')
        assert result == """<div class='prop'>
<span class='name'><label for='test'>This is a test:</label></span>
<span class='value' title='This is a test'><input type='tel' name='test' value='' id='test' size='15' maxlength='15'/><span class='units'></span><span class='required'></span><span class='hint'></span></span>
</div>"""
    }

    void testMoney() {
        formElement.form("This is a test", [money: 5, map: 'test'], null) {
            "123.00"
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result.contains('class=\'money\'')
        assert result == """<div class='prop'>
<span class='name'><label for='test'>This is a test:</label></span>
<span class='value' title='This is a test'>\$&nbsp;<input type='text' name='test' value='' id='test' class='money' size='5' maxlength='5'/><span class='units'></span><span class='required'></span><span class='hint'></span></span>
</div>"""
    }

    void testDate() {
        formElement.form("This is a test", [date: "d/M/yyyy", map: 'test'], null) {
            "01/01/2013"
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result.contains('class=\'date\'')
        assert result == """<div class='prop'>
<span class='name'><label for='test'>This is a test:</label></span>
<span class='value' title='This is a test'><input type='text' name='test' value='' class='date' size='10' id='test'/><span class='required'></span><span class='hint'></span></span>
</div>"""
    }

    void testDatetime() {
        formElement.form("This is a test", [datetime: "d/M/yyyy", map: 'test'], null) {
            "01/01/2013"
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result.contains('class=\'date\'')
        assert result == """<div class='prop'>
<span class='name'><label for='test'>This is a test:</label></span>
<span class='value' title='This is a test'><input type='text' name='test.date' value='' class='date' size='10' id='test.date'/>&nbsp;<input type='text' name='test.time' value='' class='time' size='7' title='type or use mouse wheel to change' id='test.time'/><span class='required'></span><span class='hint'></span></span>
</div>"""
    }

    void testAttachment() {
        formElement.form("This is a test", [attachment: 'document', map: 'test'], null) {}
        formElement.attr.name = 'test.document'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class='prop'>
<span class='name'><label for='test.document'>This is a test:</label></span>
<span class='value' title='This is a test'><input type='file' name='test.document' value='null' id='test.document'/>&nbsp; <span class='required'></span><span class='hint'></span></span>
</div>"""
    }

    void testSelect() {
        formElement.form("This is a test", [select: ['one','two','three','four','five'], map: 'test', default: 'one'], null) {}
        formElement.attr.name = 'test.select'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        println result
        assert result == """<div class='prop'>
<span class='name'><label for='test.select'>This is a test:</label></span>
<span class='value' title='This is a test'><select name='test.select' id='test.select'>
  <option selected='selected'>one</option>
  <option>two</option>
  <option>three</option>
  <option>four</option>
  <option>five</option>
</select> <span class='required'></span><span class='hint'></span></span>
</div>"""
    }

    void testSelectPreambleAndHint() {
        formElement.form("This is a test", [select: ['one','two','three','four','five'], map: 'test', default: 'one', hint: 'pick one of those', preamble: 'do this now', required: true], null) {}
        formElement.attr.name = 'test.select'

        String result = applyTemplate('<form:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        println result
        assert result == """<div class='preamble'>do this now</div><div class='prop'>
<span class='name'><label for='test.select'>This is a test:</label></span>
<span class='value' title='This is a test'><select name='test.select' id='test.select'>
  <option selected='selected'>one</option>
  <option>two</option>
  <option>three</option>
  <option>four</option>
  <option>five</option>
</select> <span class='required'>*</span><span class='hint'>pick one of those</span></span>
</div>"""
    }

}
