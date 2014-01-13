package com.nerderg.goodForm

import com.nerderg.goodForm.form.Form
import net.htmlparser.jericho.Source
import net.htmlparser.jericho.SourceFormatter

/**
 * Provides GoodForm-specific tag elements. The main tag elements used by views are:
 *
 * <ul>
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

    static namespace = "gf"

    private static final Map<String, Closure> elementClosures = [:]

    FormTagLib() {
        elementClosures.put('heading', heading)
        elementClosures.put('text', wrapper)
        elementClosures.put('number', wrapper)
        elementClosures.put('phone', wrapper)
        elementClosures.put('money', wrapper)
        elementClosures.put('select', wrapper)
        elementClosures.put('date', wrapper)
        elementClosures.put('datetime', wrapper)
        elementClosures.put('attachment', wrapper)
        elementClosures.put('group', group)
        elementClosures.put('pick', group)
        elementClosures.put('each', each)
        elementClosures.put('listOf', listOf)
        elementClosures.put('bool', bool)
    }

    def element = { attrs ->
        if(!attrs.templateDir) {
            attrs.templateDir = 'input'
        }
        Map model = goodFormService.getElementModel(attrs)
        Closure c = elementClosures[model.type]
        c.call(model, attrs)
    }

    private Closure heading = { Map model, Map attrs ->
        gfRender(template: "/goodFormTemplates/$attrs.templateDir/type_heading", model: model)
    }

    private Closure wrapper = { Map model, Map attrs ->
        gfRender(template: "/goodFormTemplates/$attrs.templateDir/form_field_wrapper", model: model)
    }

    private Closure each = { Map model, Map attrs ->
        gfRender(template: "/goodFormTemplates/$attrs.templateDir/form_group_top", model: model)
        goodFormService.processEachFormElement(attrs.element, attrs.store) { Map subMap ->
            subMap.disabled = attrs.disabled
            subMap.templateDir = attrs.templateDir
            out << element(subMap)
        }
        gfRender(template: "/goodFormTemplates/$attrs.templateDir/form_group_tail", model: model)
    }

    private Closure group = { Map model, Map attrs ->
        gfRender(template: "/goodFormTemplates/$attrs.templateDir/form_group_top", model: model)
        renderSubElements(attrs)
        gfRender(template: "/goodFormTemplates/$attrs.templateDir/form_group_tail", model: model)
    }

    private Closure listOf = { Map model, Map attrs ->
        gfRender(template: "/goodFormTemplates/$attrs.templateDir/form_list_top", model: model)
        for (int i = 0; i < Math.max(model.listSize as Integer, 1); i++) {
            gfRender(template: "/goodFormTemplates/$attrs.templateDir/form_list_item_top", model: model)
            attrs.index = i
            renderSubElements(attrs)
            gfRender(template: "/goodFormTemplates/$attrs.templateDir/form_list_item_tail", model: model)
        }
        gfRender(template: "/goodFormTemplates/$attrs.templateDir/form_list_tail", model: model)
    }

    private Closure bool = { Map model, Map attrs ->
        if (attrs.element.subElements.size() > 0) {
            gfRender(template: "/goodFormTemplates/$attrs.templateDir/form_reveal_top", model: model)
            renderSubElements(attrs)
            gfRender(template: "/goodFormTemplates/$attrs.templateDir/form_reveal_tail", model: model)
        } else {
            gfRender(template: "/goodFormTemplates/$attrs.templateDir/form_field_wrapper", model: model)
        }
    }

    private renderSubElements(Map attrs) {
        attrs.element.subElements.each { sub ->
            out << element([element: sub, store: attrs.store, index: attrs.index, disabled: attrs.disabled, templateDir: attrs.templateDir])
        }
    }

    private gfRender(Map params) {
        out << g.render(params)
    }

    def tidy = { attr ->
        Source source = new Source(attr.text as String)
//        SourceCompactor compactor = new SourceCompactor(source)
//        Source prettySource = new Source(compactor.toString())
        SourceFormatter sf = source.getSourceFormatter()
        out << sf.toString()
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

                if (attrs.readOnly) {
                    out << "<div class='qsetReadOnly' style='page-break-inside: avoid;'>"
                } else {
                    out << "<div class='qset' title='"
                    out << g.message(code: "goodform.click.edit")
                    out << "' id='${formInstance.id}/${i}' data-backurl='${g.createLink(action: 'back')}/${formInstance.id}/${i}'>"
                    out << "<div class='clickToEdit'>${g.message(code: "goodform.click.edit")}</div>"
                }
                goodFormService.withQuestions(qSet, questions) { q, qRef ->
                    out << element([element: q.formElement, store: formData, disabled: true, templateDir: 'display'])
                }
                out << "</div>"
                i--
            }
            log.debug "end display tag"
        }
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
