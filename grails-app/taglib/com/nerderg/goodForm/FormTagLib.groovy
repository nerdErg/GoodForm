package com.nerderg.goodForm

import com.nerderg.goodForm.form.Form
import com.nerderg.goodForm.form.FormElement

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

    //todo move to goodFormService?
    private Map getDefaultModelProperties(FormElement e, Map store, Integer index, boolean disabled, Map fieldAttributes) {
        def value = findFieldValue(store, e.attr.name, index) ?: (e.attr.default ?: '')
        String type = goodFormService.getElementType(e)
        String error = getFieldErrors(store, e.attr.name, index)

        if (e.attr.suggest) {
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

        if (!e.attr[type]) {
            return [:]
        }

        Integer size
        if (e.attr[type] instanceof Range) {
            size = e.attr[type].to.toString().size()
        } else if ((e.attr[type] as String).isInteger()) {
            size = e.attr[type].toInteger()
        }

        if (size) {
            return [size: size, maxlength: size]
        } else {
            return [:]
        }
    }

    //todo delete?
    private static String toStringIfNotNull(value) {
        value != null ? value.toString() : null
    }

    def heading = { FormElement e, Map store, Integer index, boolean disabled ->
        Map model = [fieldAttributes: [size: e.attr.heading, value: e.text]]
        out << g.render(template: "/goodFormTemplates/type_heading", model: model)

    }

    def text = { FormElement e, Map store, Integer index, boolean disabled ->
        Map model = getDefaultModelProperties(e, store, index, disabled, [:])
        out << g.render(template: "/goodFormTemplates/form_field_wrapper", model: model)
    }

    def number = { FormElement e, Map store, Integer index, boolean disabled ->
        Map model = getDefaultModelProperties(e, store, index, disabled, [:])
        out << g.render(template: "/goodFormTemplates/form_field_wrapper", model: model)
    }

    def phone = { FormElement e, Map store, Integer index, boolean disabled ->
        Map model = getDefaultModelProperties(e, store, index, disabled, [:])
        out << g.render(template: "/goodFormTemplates/form_field_wrapper", model: model)
    }

    def money = { FormElement e, Map store, Integer index, boolean disabled ->
        Map model = getDefaultModelProperties(e, store, index, disabled, [step: '0.01'])
        out << g.render(template: "/goodFormTemplates/form_field_wrapper", model: model)
    }

    def select = { FormElement e, Map store, Integer index, boolean disabled ->
        Map model = getDefaultModelProperties(e, store, index, disabled, [options : e.attr.select as List])
        out << g.render(template: "/goodFormTemplates/form_field_wrapper", model: model)
    }


    def date = { FormElement e, Map store, Integer index, boolean disabled ->
        String format = e.attr.date
        Map model = getDefaultModelProperties(e, store, index, disabled, [format: format, size: format.size()])
        out << g.render(template: "/goodFormTemplates/form_field_wrapper", model: model)
    }

    def datetime = { FormElement e, Map store, Integer index, boolean disabled ->
        String format = e.attr.datetime
        Map model = getDefaultModelProperties(e, store, index, disabled, [format: format, size: format.size()])
        out << g.render(template: "/goodFormTemplates/form_field_wrapper", model: model)
    }

    def attachment = { FormElement e, Map store, Integer index, boolean disabled ->

        Map model = getDefaultModelProperties(e, store, index, disabled, [:])
        List<String> fieldSplit = e.attr.name.split(/\./)
        Integer prefix = "${fieldSplit[0]}.${fieldSplit.last()}-".size()
        String value = model.fieldAttributes.value
        String fileName = (value && value.size() > prefix) ? value.substring(prefix) : ''
        model.fieldAttributes.fileName = fileName
        if (fileName && model.fieldAttributes.required) {
            model.fieldAttributes.remove('required')
        }
        out << g.render(template: "/goodFormTemplates/form_field_wrapper", model: model)

    }

    def pick = { FormElement e, Map store, Integer index, boolean disabled ->
        group.call(e, store, index, disabled)
    }

    def group = { FormElement e, Map store, Integer index, boolean disabled ->
        Map model = getDefaultModelProperties(e, store, index, disabled, [:])
        out << g.render(template: "/goodFormTemplates/form_group_top", model: model)
        e.subElements.each { sub ->
            out << element([element: sub, store: store, index: index, disabled: disabled])
        }
        out << g.render(template: "/goodFormTemplates/form_group_tail", model: model)
    }

    def each = { FormElement e, Map store, Integer index, boolean disabled ->

        Map model = getDefaultModelProperties(e, store, index, disabled, [:])
        out << g.render(template: "/goodFormTemplates/form_group_top", model: model)
        goodFormService.processEachFormElement(e, store) { Map subMap ->
            subMap.disabled = disabled
            out << element(subMap)
        }
        out << g.render(template: "/goodFormTemplates/form_group_tail", model: model)
    }

    def listOf = { FormElement e, Map store, Integer index, boolean disabled ->

        Map model = getDefaultModelProperties(e, store, index, disabled, [:])
        int currentListSize = listSize(store, e.attr.name)

        out << g.render(template: "/goodFormTemplates/form_list_top", model: model)
        for (int i = 0; i < Math.max(currentListSize, 1); i++) {
            out << g.render(template: "/goodFormTemplates/form_list_item_top", model: model)
            e.subElements.each { sub ->
                out << element([element: sub, store: store, index: i, disabled: disabled])
            }
            out << g.render(template: "/goodFormTemplates/form_list_item_tail", model: model)
        }
        out << g.render(template: "/goodFormTemplates/form_list_tail", model: model)
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

        Boolean pick1 = '1' == e.parent?.attr?.pick?.toString()
        Map model = getDefaultModelProperties(e, store, index, disabled, [pick1: pick1, parentName: e.parent.attr.name])
        if (model.fieldAttributes.value) {
            model.fieldAttributes.checked = 'checked'
        }

        if (e.subElements.size() > 0) {
            if(!pick1 && findFieldValue(store, "${e.attr.name}.yes", index)) {
                //need to get the renamed value as the name space isn't nested. which affects if it is checked
                model.fieldAttributes.checked = 'checked'
            }
            out << g.render(template: "/goodFormTemplates/form_reveal_top", model: model)
            e.subElements.each { sub ->
                out << element([element: sub, store: store, index: index, disabled: disabled])
            }
            out << g.render(template: "/goodFormTemplates/form_reveal_tail", model: model)
        } else {
            out << g.render(template: "/goodFormTemplates/form_field_wrapper", model: model)
        }
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
                    String qa = goodFormService.decodeFormElementAnswer(q.formElement, formData) { label, value, units, indent, type ->
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
            String qa = goodFormService.decodeFormElementAnswer(q.formElement, formData) { label, value, units, indent ->
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
