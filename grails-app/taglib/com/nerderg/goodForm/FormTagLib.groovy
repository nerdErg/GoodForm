package com.nerderg.goodForm

import com.nerderg.goodForm.form.Form
import com.nerderg.goodForm.form.FormElement
import groovy.xml.MarkupBuilder

import java.text.SimpleDateFormat

/**
 * Provides GoodForm-specific tag elements. The main tag elements used by views are:
 *
 * <ul>
 * <li>{@link FormTagLib#display} - </li>
 * <li>{@link FormTagLib#displayText} - </li>
 * <li>{@link FormTagLib#answered} - </li>
 * <li>{@link FormTagLib#element} - </li>
 * </ul>
 *
 * @author Peter McNeil
 */
class FormTagLib {

    def formDataService
    def goodFormService

    static namespace = "form"

    private static getFieldErrors(Map formData, String field, Integer index) {
        return formData.fieldErrors[field + (index == null ? '0' : index)]
    }

    private findFieldValue(Map bean, String field, Integer index = null) {
        goodFormService.findField(bean, field, index)
    }

    def element = { attrs ->
        FormElement e = attrs.element
        Map store = attrs.store
        Integer index = attrs.index
        boolean disabled = attrs.disabled ?: false

        String type = goodFormService.getElementType(e)

        "$type"(e, store, index, disabled)
    }

    private preamble(out, text) {
        if (text) {
            out << "<div class='preamble'>${text.encodeAsHTML()}</div>"
        }
    }

    def select = { FormElement e, Map store, Integer index, boolean disabled ->

        def value = findFieldValue(store, e.attr.name, index) ?: (e.attr.default ?: '')
        String error = getFieldErrors(store, e.attr.name, index)
        List<String> options = e.attr.select as List
        preamble(out, e.attr.preamble)

        StringWriter sw = new StringWriter()
        MarkupBuilder builder = new MarkupBuilder(new PrintWriter(sw))
        Map attrs = [name: e.attr.name, id: e.attr.name]
        if (disabled) {
            attrs << [disabled: 'disabled']
        }
        builder.select(attrs) {
            for (String opt in options) {
                if (opt == value) {
                    option selected: 'selected', opt
                } else {
                    option opt
                }
            }
        }
        String select = sw.toString()
        out << nerderg.formfield(label: e.text, field: e.attr.name, bean: store, error: error) {
            "$select <span class='required'>${e.attr.required ? '*' : ''}</span><span class='hint'>${e.attr.hint ? e.attr.hint : ''}</span>"
        }
    }

    def heading = { FormElement e, Map store, Integer index, boolean disabled ->
        def value = e.attr.heading

        String tag = "<h$value>${e.text.encodeAsHTML()}</h$value>"
        out << tag
    }

    //todo move to goodFormService?
    private Map getDefaultModelProperties(FormElement e, Map store, Integer index, boolean disabled, Map fieldAttributes) {
        def value = findFieldValue(store, e.attr.name, index) ?: (e.attr.default ?: '')
        String type = goodFormService.getElementType(e)
        String error = getFieldErrors(store, e.attr.name, index)

        if(e.attr.suggest) {
            if (fieldAttributes.class) {
                fieldAttributes.class += " suggest ${e.attr.suggest}"
            }
        }

        fieldAttributes << [
                value: value
        ]

        fieldAttributes << makeHtmlAttributes(disabled, e, type)

        Map model = [
                type: type,
                error: error,
                name: e.attr.name,
                label: e.text,
                preamble: e.attr.preamble,
                required: e.attr.required,
                hint: e.attr.hint,
                prefix: e.attr.prefix,
                units: e.attr.units,
                fieldAttributes: fieldAttributes
        ]
        return model
    }

    //todo move to goodFormService?
    private Map makeHtmlAttributes(boolean disabled, FormElement e, String type) {

        Map fieldAttributes = formDataService.getNumberMinMax(e) //get min/max if exist
        fieldAttributes << makeFieldSizeAttributes(e, type)

        if (disabled) {
            fieldAttributes << [disabled: 'disabled']
        }

        if (e.attr.required) {
            fieldAttributes << [required: 'required']
        }

        if (e.attr.pattern) {
            fieldAttributes << [pattern: e.attr.pattern[0]]
            fieldAttributes << [title: e.attr.pattern[1]]
        }
        return fieldAttributes
    }

    //todo move to goodFormService?
    private static Map makeFieldSizeAttributes(FormElement e, String type) {

        if(!e.attr[type]){
            return [:]
        }

        Integer size
        if (e.attr[type] instanceof Range) {
            size = e.attr[type].to.toString().size()
        } else if((e.attr[type] as String).isInteger()) {
            size = e.attr[type].toInteger()
        }

        if(size) {
            return [size: size, maxlength: size]
        } else {
            return [:]
        }
    }

    //todo delete?
    private static String toStringIfNotNull(value) {
        value != null ? value.toString() : null
    }

    def text = { FormElement e, Map store, Integer index, boolean disabled ->
        Map model = getDefaultModelProperties(e, store, index, disabled, [:])
        out << g.render(template: "/templates/form_field_wrapper", model: model)
    }

    def number = { FormElement e, Map store, Integer index, boolean disabled ->
        Map model = getDefaultModelProperties(e, store, index, disabled, [:])
        out << g.render(template: "/templates/form_field_wrapper", model: model)
    }

    def phone = { FormElement e, Map store, Integer index, boolean disabled ->
        Map model = getDefaultModelProperties(e, store, index, disabled, [:])
        out << g.render(template: "/templates/form_field_wrapper", model: model)
    }

    def money = { FormElement e, Map store, Integer index, boolean disabled ->
        Map model = getDefaultModelProperties(e, store, index, disabled, [step : '0.01'] )
        out << g.render(template: "/templates/form_field_wrapper", model: model)
    }

    def date = { FormElement e, Map store, Integer index, boolean disabled ->
        String format = e.attr.date
        Map model = getDefaultModelProperties(e, store, index, disabled, [format: format, size: format.size()])
        out << g.render(template: "/templates/form_field_wrapper", model: model)
    }

    def datetime = { FormElement e, Map store, Integer index, boolean disabled ->
        String format = e.attr.datetime
        Map model = getDefaultModelProperties(e, store, index, disabled, [format: format, size: format.size()])
        out << g.render(template: "/templates/form_field_wrapper", model: model)
    }

    def attachment = { FormElement e, Map store, Integer index, boolean disabled ->

        Map model = getDefaultModelProperties(e, store, index, disabled, [:])
        List<String> fieldSplit = e.attr.name.split(/\./)
        Integer prefix = "${fieldSplit[0]}.${fieldSplit.last()}-".size()
        String value = model.fieldAttributes.value
        String fileName = (value && value.size() > prefix) ? value.substring(prefix) : ''
        model.fieldAttributes.fileName = fileName
        if(fileName && model.fieldAttributes.required) {
            model.fieldAttributes.remove('required')
        }
        out << g.render(template: "/templates/form_field_wrapper", model: model)

    }

    def pick = { FormElement e, Map store, Integer index, boolean disabled ->
        group.call(e, store, index, disabled)
    }

    def group = { FormElement e, Map store, Integer index, boolean disabled ->
        Map model = getDefaultModelProperties(e, store, index, disabled, [:])
        out << g.render(template: "/templates/form_group_top", model: model)
        e.subElements.each { sub ->
            out << element([element: sub, store: store, index: index, disabled: disabled])
        }
        out << g.render(template: "/templates/form_group_tail", model: model)
    }

    def each = { FormElement e, Map store, Integer index, boolean disabled ->

        Map model = getDefaultModelProperties(e, store, index, disabled, [:])
        out << g.render(template: "/templates/form_group_top", model: model)
        goodFormService.processEachFormElement(e, store) { Map subMap ->
            subMap.disabled = disabled
            out << element(subMap)
        }
        out << g.render(template: "/templates/form_group_tail", model: model)
    }

    /**
     *
     */
    def listOf = { FormElement e, Map store, Integer index, boolean disabled ->

        out << "<h2>$e.text <span class='hint'>${e.attr.hint ? e.attr.hint : ''}</span></h2>"
        preamble(out, e.attr.preamble)
        out << "<div class='listContainer'>"

        def currentListSize = listSize(store, e.attr.name)

        for (int i = 0; i < Math.max(currentListSize, 1); i++) {
            out << "<div class='questionListOfItem'>"
            e.subElements.each { sub ->
                out << element([element: sub, store: store, index: i, disabled: disabled])
            }
            out << "<div class='removeForm'><img src='"
            out << g.resource(dir: 'images/icons', file: 'delete.png')
            out << """' title='Remove this' alt='+'/><span class='hint'>"""
            out << g.message(code: "goodform.item.remove")
            out << "</span></div>"
            out << "</div>"
        }
        out << "</div>"

        out << "<div class='addAnotherForm'><img src='"
        out << g.resource(dir: 'images/icons', file: 'add.png')
        out << "' title='add another' alt='+'/><span class='hint'>"
        out << g.message(code: "goodform.item.addAnother")
        out << "</span></div>"

    }

    private int listSize(Map store, String listName) {
        def value = findFieldValue(store, listName)
        if (value && value instanceof Map) {
            def l = value.find { entry ->
                formDataService.isCollectionOrArray(entry.value)
            }
            return l ? l.value.size() : 0
        } else {
            return 0
        }
    }

    def bool = { FormElement e, Map store, Integer index, boolean disabled ->

        String pick = e.parent?.attr?.pick?.toString()
        Map model = getDefaultModelProperties(e, store, index, disabled, [pick1: ('1' == pick), parentName: e.parent.attr.name])
        if(model.fieldAttributes.value && (model.fieldAttributes.value == 'on' || model.fieldAttributes.value == model.label)) {
            model.fieldAttributes.checked = 'checked'
        }
        out << g.render(template: "/templates/form_field_wrapper", model: model)


//        String disabledAttr = disabled ? "disabled='disabled'" : ""
//        if (e.subElements.size() > 0) {
//            if (pick && pick == "1") {
//                def value = findFieldValue(store, e.parent.attr.name, index)
//                radioHiddenSubElements(store, index, value, e, disabled)
//            } else {
//                def value = findFieldValue(store, "${e.attr.name}.yes", index)
//                checkboxHiddenSubElements(store, index, value, e, disabled)
//            }
//        } else {
//            if (pick && pick == "1") {
//                def value = findFieldValue(store, e.parent.attr.name, index)
//                radioButtonElement(value, e, disabledAttr)
//            } else {
//                def value = findFieldValue(store, e.attr.name, index)
//                checkboxElement(value, e, store, disabledAttr)
//            }
//        }
    }

    private radioButtonElement(value, FormElement e, disabledAttr) {
        String buttonValue = e.text.encodeAsHTML().replaceAll(/'/, '&rsquo;')
        if (value == e.text.replaceAll(/'/, '\u2019')) {
            out << nerderg.formfield(label: e.text, field: e.attr.name) {
                """<input type='radio' name='$e.parent.attr.name' id='$e.attr.name' value='$buttonValue' checked='checked' ${
                    disabledAttr
                }/>
                    <span class='required'>${e.attr.required ? '*' : ''}</span><span class='hint'>${
                    e.attr.hint ? e.attr.hint : ''
                }</span>
                    """
            }
        } else {
            out << nerderg.formfield(label: e.text, field: e.attr.name) {
                """<input type='radio' name='$e.parent.attr.name' id='$e.attr.name' value='$buttonValue' ${disabledAttr}/>
                    <span class='required'>${e.attr.required ? '*' : ''}</span><span class='hint'>${
                    e.attr.hint ? e.attr.hint : ''
                }</span>
                    """
            }
        }
    }
    private checkboxElement(value, FormElement e, Map store, disabledAttr) {
        out << "<div class='inlineCheck'>"

        if (value == 'on') {
            out << nerderg.formfield(label: e.text, field: e.attr.name, bean: store, 'class': 'inlineCheck') {
                """<span class='preamble'>${e.attr.preamble ?: ''}</span><span class='cbText'>${
                    e.attr.text ? '*' : ''
                }</span><input type='checkbox' name='$e.attr.name' id='$e.attr.name' checked='checked' ${disabledAttr}/>
                    <span class='required'>${e.attr.required ? '*' : ''}</span><span class='hint'>${
                    e.attr.hint ? e.attr.hint : ''
                }</span>
                    """
            }
        } else {
            out << nerderg.formfield(label: e.text, field: e.attr.name, bean: store, 'class': 'inlineCheck') {
                """<span class='preamble'>${
                    e.attr.preamble ?: ''
                }</span><input type='checkbox' name='$e.attr.name' id='$e.attr.name' ${disabledAttr}/>
                    <span class='required'>${e.attr.required ? '*' : ''}</span><span class='hint'>${
                    e.attr.hint ? e.attr.hint : ''
                }</span>
                    """
            }
        }
        out << "</div > "
    }


    //output a sub elements as a hidden option that becomes visible on clicking the checkbox
    private checkboxHiddenSubElements(store, index, value, FormElement e, boolean disabled) {
        def disabledAttr = disabled ? "disabled='disabled'" : ""
        def name = "${e.attr.name}.yes"

        if (value == 'on') {
            preamble(out, e.attr.preamble)
            out << "<div class='inlineCheck'>"
            out << nerderg.formfield(label: e.text, field: e.attr.name, bean: store) {
                """<input type='checkbox' name='${
                    name
                }' id='$e.attr.name' class='hiddenFormCheckbox' checked='checked' ${disabledAttr}/>
                <span class='required'>${e.attr.required ? '*' : ''}</span><span class='hint'>${
                    e.attr.hint ? e.attr.hint : ''
                }</span>
                """
            }
            out << '</div>'   //inline
            out << "<div>"
        } else {
            preamble(out, e.attr.preamble)
            out << "<div class='inlineCheck'>"
            out << nerderg.formfield(label: e.text, field: e.attr.name, bean: store) {
                """<input type='checkbox' name='${name}' id='$e.attr.name' class='hiddenFormCheckbox' ${disabledAttr}/>
                <span class='required'>${e.attr.required ? '*' : ''}</span><span class='hint'>${
                    e.attr.hint ? e.attr.hint : ''
                }</span>
                """
            }
            out << '</div>'  //inline
            out << "<div class='hiddenForm'>"
        }

        out << "<div class='dependantQuestions'>"

        e.subElements.each { sub ->
            out << element([element: sub, store: store, index: index, disabled: disabled])
        }
        out << "</div></div>"

    }

    //output a sub elements as a hidden option that becomes visible on clicking the checkbox
    private radioHiddenSubElements(store, index, value, FormElement e, boolean disabled) {
        String disabledAttr = disabled ? "disabled='disabled'" : ""
        String buttonValue = e.text.encodeAsHTML().replaceAll(/'/, '&rsquo;')

        if (value == e.text.replaceAll(/'/, '\u2019')) {  //todo check if this should be buttonValue
            out << nerderg.formfield(label: e.text, field: e.attr.name, bean: store) {
                """<input type='radio' name='${e.parent.attr.name}' id='$e.attr.name' value='${
                    buttonValue
                }' class='hiddenFormRadio' checked='checked' ${disabledAttr}/>
                <span class='required'>${e.attr.required ? '*' : ''}</span><span class='hint'>${
                    e.attr.hint ? e.attr.hint : ''
                }</span>
                """
            }
            out << "<div>"
        } else {
            out << nerderg.formfield(label: e.text, field: e.attr.name, bean: store) {
                """<input type='radio' name='${e.parent.attr.name}' id='$e.attr.name' value='${
                    buttonValue
                }' class='hiddenFormRadio' ${disabledAttr}/>
                <span class='required'>${e.attr.required ? '*' : ''}</span><span class='hint'>${
                    e.attr.hint ? e.attr.hint : ''
                }</span>
                """
            }
            out << "<div class='hiddenForm'>"
        }

        out << "<div class='dependantQuestions'>"

        e.subElements.each { sub ->
            out << element([element: sub, store: store, index: index, disabled: disabled])
        }
        out << "</div></div>"

    }

    /**
     *
     */
    def answered = { attrs ->
        FormInstance formInstance = attrs.formInstance
        Map formData = attrs.store
        Form questions = formDataService.getFormQuestions(formInstance.formVersion)

        List state = formInstance.storedState().reverse()
        List currentQuestions = formInstance.storedCurrentQuestion()
        boolean found = false
        def i = 0
        state.each() { List qSet ->
            if (found) {
                out << "<div class='qset' title='"
                out << g.message(code: "goodform.click.edit")
                out << "' id='${formInstance.id}/${i}' data-backurl='${g.createLink(action: 'back')}/${formInstance.id}/${i}'>"
                out << "<div class='clickToEdit'>${g.message(code: "goodform.click.edit")}</div>"
                out << "<div class='qsetDisplay'>${qSet.toString()}</div>"
                goodFormService.withQuestions(qSet, questions) { q, qRef ->
                    out << element([element: q.formElement, store: formData, disabled: true])
                }
                out << "</div>"
            }
            i++
            found = found || qSet == currentQuestions
        }
    }

    def display = { attrs ->
        log.debug "in display tag $attrs"
        FormInstance formInstance = attrs.formInstance
        Map formData = attrs.store
        Form questions = formDataService.getFormQuestions(formInstance.formVersion)

        List state = formInstance.storedState()
        def i = state.size() - 1
        state.each() { List qSet ->
            out << "<div class='qset' title='"
            out << g.message(code: "goodform.click.edit")
            out << "' id='${formInstance.id}/${i}' data-backurl='${g.createLink(action: 'back')}/${formInstance.id}/${i}'>"
            out << "<div class='clickToEdit'>${g.message(code: "goodform.click.edit")}</div>"
            goodFormService.withQuestions(qSet, questions) { q, qRef ->
                out << element([element: q.formElement, store: formData, disabled: true])
            }
            out << "</div>"
            i--
        }
        log.debug "end display tag"
    }

    /**
     * Displays a text version of the form and data to view
     */
    def displayText = { attrs ->
        log.debug "in display tag $attrs"
        FormInstance formInstance = attrs.formInstance
        Map formData = attrs.store
        Form questions = formDataService.getFormQuestions(formInstance.formVersion)

        List state = formInstance.storedState()
        def i = state.size() - 1
        state.each() { List qSet ->
            if (!qSet.isEmpty() && qSet[0] != 'End') {
                List output = []
                Boolean contentPresent = false

                goodFormService.withQuestions(qSet, questions) { q, qRef ->
                    String qa = goodFormService.printFormElementAnswer(q.formElement, formData) { label, value, units, indent, type ->
                        if (type == 'heading') {
                            String res
                            switch (value) {
                                case 1:
                                    res = "<h1>${label.encodeAsHTML()}</h1>"
                                    break
                                case 2:
                                    res = "<h3>${label.encodeAsHTML()}</h3>"
                                    break
                                default:
                                    res = "<h3>${label.encodeAsHTML()}</h3>"
                            }
                            return res

                        } else {
                            Boolean answerPresent = (value && value != 'No')
                            contentPresent = contentPresent || answerPresent

                            String res = "<div class='goodformView'>"
                            res += "${indent.replaceAll(' ', '&nbsp;')}"
                            res += "${label ? "<span class='label'>${label.encodeAsHTML()}:</span>" : ''} "
                            if (value) {
                                if (value instanceof String && value.contains("\n")) {
                                    res += "<div class='textNote'>${value.encodeAsHTML()}</div>"
                                } else {
                                    res += "${value.encodeAsHTML()}"
                                }
                            } else {
                                res += " - "
                            }
                            res += "&nbsp;${units ? units.encodeAsHTML() : ''}</div>"
                            return res
                        }
                    }
                    output.add(qa)
                }


                if (contentPresent) {
                    if (attrs.readOnly) {
                        out << "<div class='qsetReadOnly' style='page-break-inside: avoid;'>"
                    } else {
                        out << "<div class='qset' title='"
                        out << g.message(code: "goodform.click.edit")
                        out << "' id='${formInstance.id}/${i}' data-backurl='${g.createLink(action: 'back')}/${formInstance.id}/${i}'>"
                        out << "<div class='clickToEdit'>${g.message(code: "goodform.click.edit")}</div>"
                    }
                    output.each { out << it }
                    out << "</div>"
                }
                i--
            }
            log.debug "end display tag"
        }
    }

    /**
     *
     */
    def displayFilteredText = { attrs ->
        log.debug "in display tag $attrs"
        FormInstance formInstance = attrs.formInstance
        Map formData = attrs.store
        Form questions = formDataService.getFormQuestions(formInstance.formVersion)
        Boolean compress = attrs.compress || attrs.readOnly

        List refs = attrs.refs as List
        refs.sort { a, b -> a<=>b }
        List output = []

        goodFormService.withQuestions(refs, questions) { q, qRef ->
            String qa = goodFormService.printFormElementAnswer(q.formElement, formData) { label, value, units, indent ->
                if (value && value != 'No') {
                    String res = "<div class='goodformView'>"
                    res += "${indent.replaceAll(' ', '&nbsp;')}"
                    res += "<span class='label'>${label ? label.encodeAsHTML() + ':' : ''}</span> "
                    if (value) {
                        if (value instanceof String && value.contains("\n")) {
                            res += "<div class='textNote'>${value.encodeAsHTML()}</div>"
                        } else {
                            res += "<span class='value'>${value.encodeAsHTML()}</span>"
                        }
                    } else {
                        res += " - "
                    }
                    res += "${units ? units.encodeAsHTML() : ''}</div>"
                    return res
                }
                return ''
            }
            output.add(qa)
        }

        def contentPresent = output.find { it != '' }

        if (!compress || contentPresent) {
            out << "<div class='paddedBox' style='page-break-inside: avoid;'>"
            if (contentPresent) {
                output.each { out << it }
            } else {
                out << g.message(code: "goodform.no.answers")
            }
            out << "</div>"
        }
        log.debug "end display tag"
    }

    //todo delete if we're not using this
    def displayFormData = { attrs ->
        log.debug "in display tag $attrs"
        FormInstance formInstance = attrs.formInstance
        Map formData = attrs.store

        List state = formInstance.storedState()
        state.each() { List qSet ->
            out << "<div class='qsetReadOnly'>"
            goodFormService.withQuestions(qSet, formData as Map) { qData, qRef ->
                String qa = goodFormService.printFormDataAnswer(qData as Map, '') { label, value, units, indent ->
                    if (value && value != 'No') {
                        String res = "<div title='$qRef'>"
                        res += "${indent.replaceAll(' ', '&nbsp;')}${label ? "<b>${label.encodeAsHTML()}:</b>" : ''} ${value ? value.encodeAsHTML() : '-'} ${units ? units.encodeAsHTML() : ''}</div>"
                        return res
                    }
                    return ''
                }
                out << qa
            }
            out << "</div>"
        }
        log.debug "end display tag"
    }

    def linkToQset = { attrs, body ->
        log.debug "in linkToQset tag $attrs"
        FormInstance formInstance = attrs.formInstance
        String questionRef = attrs.questionRef
        List<List> state = formInstance.storedState()
        def i = 0
        while (i < state.size() && !(state[i].contains(questionRef))) {
            i++
            log.debug "$i -> ${state[i]}"
        }
        i = state.size() - i - 1
        String href = g.createLink(action: "back") + "/${formInstance.id}/${i}"
        out << "<a href='$href'>"
        out << body()
        out << '</a>'
    }

    def showMessages = { attrs ->

        if (attrs.fieldErrors) {
            out << '<div class="errors">'
            out << g.message(code: 'goodform.field.errors', args: [attrs.fieldErrors.size().toString()])
            out << '</div>'
        }
        if (flash.message) {
            out << '<div class="message">'
            if (formDataService.isCollectionOrArray(flash.message)) {
                out << '<ul>'
                flash.message.each { item ->
                    out << '<li>' + item.toString().encodeAsHTML() + '</li>'
                }

            } else {
                out << flash.message.toString().encodeAsHTML()
            }
            out << '</ul></div>'
        }
    }

}
