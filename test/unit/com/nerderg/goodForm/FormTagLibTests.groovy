package com.nerderg.goodForm

import com.nerderg.goodForm.form.FormElement
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
        formElement = new FormElement('q1')
        tagLib.goodFormService = new GoodFormService()
        tagLib.formDataService = new FormDataService()
    }

    void testGroup() {
        FormElement subElement = new FormElement('s1')
        subElement.form("Dummy element", [text: 10, map: 'test'], null) {
            "testing"
        }
        formElement.form("Dummy element", [group: 'test', map: 'test'], null) {
            "testing"
        }
        formElement.subElements.add(subElement)
        formElement.attr.name = 'test'
        subElement.attr.name = 'test.test'

        String result = tagLib.tidy(text: applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim())
        assert result == """<div class="formField " title="">
\t<fieldset name="test" title="Dummy element"
\t\t>
\t\t<legend class="">
\t\t\tDummy element
\t\t</legend>
\t\t<div class="formField " title="">
\t\t\t<label for="test.test" class="">Dummy element</label>
\t\t\t<input
\t\t\t\ttype="text"
\t\t\t\tname="test.test"
\t\t\t\tid="test.test"
\t\t\t\tvalue=""
\t\t\t\tsize="10"
\t\t\t\tmaxlength="10"
\t\t\t\t/>
\t\t</div>
\t</fieldset>
</div>
"""
    }

    void testListOf() {
        FormElement subElement = new FormElement('s1')
        subElement.form("Dummy element", [text: 10, map: 'test'], null) {
            "testing"
        }
        formElement.form("Dummy element", [listOf: 'test', map: 'test'], null) {
            "testing"
        }
        formElement.subElements.add(subElement)
        formElement.attr.name = 'test'
        subElement.attr.name = 'test.test'

        String result = tagLib.tidy(text: applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim())
        assert result == """<label for="test" class="">Dummy element</label>
<div class='listContainer'>
\t<div class='questionListOfItem'>
\t\t<div class="formField " title="">
\t\t\t<label for="test.test" class="">Dummy element</label>
\t\t\t<input
\t\t\t\ttype="text"
\t\t\t\tname="test.test"
\t\t\t\tid="test.test"
\t\t\t\tvalue=""
\t\t\t\tsize="10"
\t\t\t\tmaxlength="10"
\t\t\t\t/>
\t\t</div>
\t\t<div class='removeForm'><img src='/images/icons/delete.png' title='Remove this' alt='+'/>
\t\t\t<span class='hint'>goodform.item.remove</span>
\t\t</div>
\t</div>
</div>
<div class='addAnotherForm'>
\t<img src="/images/icons/add.png" title='add another' alt='+'/>
\t<span class='hint'>goodform.item.addAnother</span>
</div>
"""
    }

    void testText() {
        formElement.form("Dummy element", [text: 10, map: 'test'], null) {
            "testing"
        }
        formElement.attr.name = 'test'

        String result = tagLib.tidy(text: applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim())
        assert result == """<div class="formField " title="">
\t<label for="test" class="">Dummy element</label>
\t<input
\t\ttype="text"
\t\tname="test"
\t\tid="test"
\t\tvalue=""
\t\tsize="10"
\t\tmaxlength="10"
\t\t/>
</div>
"""
    }

    void testLargeText() {
        formElement.form("Dummy element", [text: 200, map: 'test'], null) {
            "testing"
        }
        formElement.attr.name = 'test'

        String result = tagLib.tidy(text: applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim())
        assert result == """<div class="formField " title="">
\t<label for="test" class="">Dummy element</label>
\t<textarea
\t\tname="test"
\t\tid="test"
\t\tcols="80"
\t\trows="3"
\t\t></textarea>
</div>
"""
    }

    void testPick() {
        formElement.form("Dummy element", [pick: 1, map: 'test'], null) {
            "are you male?" default: true
        }
        formElement.attr.name = 'test'
        formElement.subElements[0].attr.name = 'test.sub'

        String result = tagLib.tidy(text: applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim())
        String expect ="""<div class="formField " title="">
\t<fieldset name="test" title="Dummy element"
\t\tsize="1"
\t\tmaxlength="1"
\t\t>
\t\t<legend class="">
\t\t\tDummy element
\t\t</legend>
\t\t<div class="formField " title="">
\t\t\t<label for="test.sub" class="">are you male?</label>
\t\t\t<input
\t\t\t\ttype="radio"
\t\t\t\tname="test"
\t\t\t\tvalue="are you male?"
\t\t\t\t/>
\t\t</div>
\t</fieldset>
</div>""".toString()
        assert result.contains('type="radio"')
        assert result.compareTo(expect)
    }

    void testHeader() {
        formElement.form("This is a test", [heading: 1, map: 'test'], null) {
            "A Test Heading"
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == '<h1>This is a test</h1>'
    }

    void testNumber() {
        formElement.form("This is a test", [number: 5, map: 'test'], null) {}
        formElement.attr.name = 'test'

        String result = tagLib.tidy(text: applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim())
        assert result == """<div class="formField " title="">
\t<label for="test" class="">This is a test</label>
\t<input
\t\ttype="number"
\t\tname="test"
\t\tid="test"
\t\tvalue=""
\t\tsize="5"
\t\tmaxlength="5"
\t\t/>
</div>
"""
    }

    void testNumberRange() {
        formElement.form("This is a test", [number: 0..21, map: 'test'], null) {}
        formElement.attr.name = 'test'

        String result = tagLib.tidy(text: applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim())
        assert result == """<div class="formField " title="">
\t<label for="test" class="">This is a test</label>
\t<input
\t\ttype="number"
\t\tname="test"
\t\tid="test"
\t\tvalue=""
\t\tmax="21"
\t\tmin="0"
\t\tsize="2"
\t\tmaxlength="2"
\t\t/>
</div>
"""
    }

    void testPhone() {
        formElement.form("This is a test", [phone: 15, map: 'test'], null) {
            "+61400123456"
        }
        formElement.attr.name = 'test'

        String result = tagLib.tidy(text: applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim())
        assert result == """<div class="formField " title="">
\t<label for="test" class="">This is a test</label>
\t<input
\t\ttype="tel"
\t\tname="test"
\t\tid="test"
\t\tvalue=""
\t\tsize="15"
\t\tmaxlength="15"
\t\t/>
</div>
"""
    }

    void testMoney() {
        formElement.form("This is a test", [money: 5, map: 'test'], null) {
            "123.00"
        }
        formElement.attr.name = 'test'

        String result = tagLib.tidy(text: applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim())
        assert result == """<div class="formField " title="">
\t<label for="test" class="">This is a test</label>
\t\$&nbsp;<input
\t\ttype="number"
\t\tname="test"
\t\tid="test"
\t\tvalue=""
\t\tsize="5"
\t\tmaxlength="5"
\t\tclass="money"
\t\t/>
</div>
"""
    }

    void testDate() {
        formElement.form("This is a test", [date: "d/M/yyyy", map: 'test'], null) {
            "01/01/2013"
        }
        formElement.attr.name = 'test'

        String result = tagLib.tidy(text: applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim())
        assert result == """<div class="formField " title="">
\t<label for="test" class="">This is a test</label>
\t<input
\t\ttype="text"
\t\tname="test"
\t\tclass="date"
\t\tformat="d/M/yyyy"
\t\tsize="10"
\t\tvalue=""
\t\t/>
</div>
"""
    }

    void testDatetime() {
        formElement.form("This is a test", [datetime: "d/M/yyyy", map: 'test'], null) {
            "01/01/2013"
        }
        formElement.attr.name = 'test'

        String result = tagLib.tidy(text: applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim())
        assert result == """<div class="formField " title="">
\t<label for="test" class="">This is a test</label>
\t<input type="text" name="test.date" id="test.date" value="" class="date"
\t\tformat="d/M/yyyy"
\t\tsize="10"
\t\t/>
\t<input type="text" name="test.time" id="test.time" value="" class="time"
\t\tformat="d/M/yyyy"
\t\tsize="10"
\t\t/>
</div>
"""
    }

    void testAttachment() {
        formElement.form("This is a test", [attachment: 'document', map: 'test'], null) {}
        formElement.attr.name = 'test.document'

        String result = tagLib.tidy(text: applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim())
        assert result == """<div class="formField " title="">
\t<label for="test.document" class="">This is a test</label>
\t<input
\t\ttype="file"
\t\tname="test.document"
\t\tid="test.document"
\t\tfileName=""
\t\t/>
\t&nbsp;
</div>
"""
    }

    void testSelect() {
        formElement.form("This is a test", [select: ['one','two','three','four','five'], map: 'test', default: 'one'], null) {}
        formElement.attr.name = 'test.select'

        String result = tagLib.tidy(text: applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim())
        assert result == """<div class="formField " title="">
\t<label for="test.select" class="">This is a test</label>
\t<select name="test.select" id="test.select"
\t\t>
\t\t<option selected="selected">one</option>
\t\t<option>two</option>
\t\t<option>three</option>
\t\t<option>four</option>
\t\t<option>five</option>
\t</select>
</div>
"""
    }

    void testSelectPreambleAndHint() {
        formElement.form("This is a test", [select: ['one','two','three','four','five'], map: 'test', default: 'one', hint: 'pick one of those', preamble: 'do this now', required: true], null) {}
        formElement.attr.name = 'test.select'

        String result = tagLib.tidy(text: applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim())
        println result
        assert result == """<div class="formField " title="">
\t<div class='preamble'>do this now</div>
\t<label for="test.select" class="">This is a test</label>
\t<select name="test.select" id="test.select"
\t\trequired="required"
\t\t>
\t\t<option selected="selected">one</option>
\t\t<option>two</option>
\t\t<option>three</option>
\t\t<option>four</option>
\t\t<option>five</option>
\t</select>
\t<span class='required'>*</span>
\t<span class='hint'>pick one of those</span>
</div>
"""
    }

}
