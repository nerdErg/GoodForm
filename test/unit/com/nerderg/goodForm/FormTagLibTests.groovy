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
        mockService(GoodFormService)
        mockService(FormValidationService)
        mockService(FormReferenceService)
        mockService(FormDataService)
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

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class="col-12-xs col-8-md col-6-lg formField form-group " title="">
\t<fieldset name="test" title="Dummy element">
\t\t<legend class="">
\t\t\tDummy element
\t\t</legend>
\t\t<div class="formField " title="">
\t\t\t<label for="test.test" class="">Dummy element</label>
\t\t\t<input type="text" name="test.test" id="test.test" maxlength="10" style="max-width: 9.5em" value="" class="form-control" />
\t\t</div>
\t</fieldset>
</div>"""
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

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<fieldset class="listFieldSet">
\t<legend>Dummy element</legend>
\t<div class="listContainer">
\t\t<div class="questionListOfItem">
\t\t\t<div class="formField " title="">
\t\t\t\t<label for="test.test" class="">Dummy element</label>
\t\t\t\t<input type="text" name="test.test" id="test.test" maxlength="10" style="max-width: 9.5em" value="" class="form-control" />
\t\t\t</div>
\t\t\t<span class="removeForm btn-xs btn-warning">
\t\t\t<img src="/images/icons/delete.png" title="Remove this" alt="-" />
\t\t\tgoodform.item.remove
\t\t\t</span>
\t\t</div>
\t</div>
\t<span class="addAnotherForm btn-xs btn-success">
\t<img src="/images/icons/add.png" title="add another" alt="+" />
\tgoodform.item.addAnother
\t</span>
</fieldset>"""
    }

    void testText() {
        formElement.form("Dummy element", [text: 10, map: 'test'], null) {
            "testing"
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class="formField " title="">
\t<label for="test" class="">Dummy element</label>
\t<input type="text" name="test" id="test" maxlength="10" style="max-width: 9.5em" value="" class="form-control" />
</div>"""
    }

    void testLargeText() {
        formElement.form("Dummy element", [
                text: 120,
                map: 'test'
        ], null) {
            "testing"
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>',
                [formElement: formElement, formData: [fieldErrors: [:], test: 'this is a\nbroken string.']]).trim()

        assert result == """<div class="formField " title="">
\t<label for="test" class="">Dummy element</label>
\t<!-- <div></div> -->
\t<textarea name="test" id="test" class="form-control">this is a
broken string.</textarea>
</div>"""
    }

    void testPick() {
        formElement.form("Dummy element", [pick: 1, map: 'test'], null) {
            "are you male?" default: true
        }
        formElement.attr.name = 'test'
        formElement.subElements[0].attr.name = 'test.sub'

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        String expect ="""<div class="col-12-xs col-8-md col-6-lg formField form-group " title="">
\t<fieldset name="test" title="Dummy element" size="1" maxlength="1">
\t\t<legend class="">
\t\t\tDummy element
\t\t</legend>
\t\t<div class="col-12-xs col-8-md col-6-lg formField form-group " title="">
\t\t\t<label class="">
\t\t\t<input type="radio" name="test" value="are you male?" class="form-control" />&nbsp;are you male?</label>
\t\t</div>
\t</fieldset>
</div>"""
        assert result.contains('type="radio"')
        assert result == expect
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

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class="formField " title="">
\t<label for="test" class="">This is a test</label>
\t<input type="number" name="test" id="test" maxlength="5" style="max-width: 6.0em" value="" class="form-control" />
</div>"""
    }

    void testNumberRange() {
        formElement.form("This is a test", [number: 0..21, map: 'test'], null) {}
        formElement.attr.name = 'test'

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class="formField " title="">
\t<label for="test" class="">This is a test</label>
\t<input type="number" name="test" id="test" min="0" maxlength="2" max="21" style="max-width: 4em" value="" class="form-control" />
</div>"""
    }

    void testPhone() {
        formElement.form("This is a test", [phone: 15, map: 'test'], null) {
            "+61400123456"
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class="formField " title="">
\t<label for="test" class="">This is a test</label>
\t<input type="tel" name="test" id="test" maxlength="15" style="max-width: 13.0em" value="" class="form-control" />
</div>"""
    }

    void testMoney() {
        formElement.form("This is a test", [money: 5, map: 'test'], null) {
            "123.00"
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class="formField " title="">
\t<label for="test" class="">This is a test</label>
\t\$&nbsp;<input type="number" name="test" id="test" maxlength="5" style="max-width: 6.0em" value="" class="money form-control" step="0.01" />
</div>"""
    }

    void testDate() {
        formElement.form("This is a test", [date: "d/M/yyyy", map: 'test'], null) {
            "01/01/2013"
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class="formField " title="">
\t<label for="test" class="">This is a test</label>
\t<input type="text" name="test" id="test" style="max-width: 9.5em" value="" class="form-control date" format="d/M/yyyy" />
</div>"""
    }

    void testDatetime() {
        formElement.form("This is a test", [datetime: "d/M/yyyy", map: 'test'], null) {
            "01/01/2013"
        }
        formElement.attr.name = 'test'

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class="formField " title="">
\t<fieldset>
\t\t<legend>This is a test</legend>
\t\t<div class="datetime">
\t\t\t<label for="test.date">Date
\t\t\t<input type="text" name="test.date" id="test.date" value="" style="max-width: 9.5em" class="form-control date" format="d/M/yyyy" />
\t\t\t</label>
\t\t\t<label for="test.time">Time
\t\t\t<input type="text" name="test.time" id="test.time" value="" style="max-width: 9.5em" class="form-control time" format="d/M/yyyy" />
\t\t\t</label>
\t\t</div>
\t\t<div class="datetime">
\t\t</div>
\t</fieldset>
</div>"""
    }

    void testAttachment() {
        formElement.form("This is a test", [attachment: 'document', map: 'test'], null) {}
        formElement.attr.name = 'test.document'

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class="formField " title="">
\t<label for="test.document" class="">This is a test</label>
\t<input type="file" name="test.document" id="test.document" fileName="" class="form-control" />
\t&nbsp;
</div>"""
    }

    void testSelect() {
        formElement.form("This is a test", [select: ['one','two','three','four','five'], map: 'test', default: 'one'], null) {}
        formElement.attr.name = 'test.select'

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class="formField " title="">
\t<label for="test.select" class="">This is a test</label>
\t<select name="test.select" id="test.select" class="form-control">
\t\t<option selected="selected">one</option>
\t\t<option>two</option>
\t\t<option>three</option>
\t\t<option>four</option>
\t\t<option>five</option>
\t</select>
</div>"""
    }

    void testSelectPreambleAndHint() {
        formElement.form("This is a test", [select: ['one','two','three','four','five'], map: 'test', default: 'one', hint: 'pick one of those', preamble: 'do this now', required: true], null) {}
        formElement.attr.name = 'test.select'

        String result = applyTemplate('<gf:element element="${formElement}" store="${formData}"/>', [formElement: formElement, formData: [fieldErrors: [:]]]).trim()
        assert result == """<div class="formField " title="">
\t<div class="preamble">do this now</div>
\t<label for="test.select" class="">This is a test</label>
\t<select name="test.select" id="test.select" class="form-control" required="required">
\t\t<option selected="selected">one</option>
\t\t<option>two</option>
\t\t<option>three</option>
\t\t<option>four</option>
\t\t<option>five</option>
\t</select>
\t<span class="required">*</span>
\t<p class="hint help-block">pick one of those</p>
</div>"""
    }

}
