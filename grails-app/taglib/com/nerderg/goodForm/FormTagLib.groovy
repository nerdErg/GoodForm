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
        elementClosures.put('datetime', datetime)
        elementClosures.put('attachment', wrapper)
        elementClosures.put('group', group)
        elementClosures.put('pick', group)
        elementClosures.put('each', each)
        elementClosures.put('listOf', listOf)
        elementClosures.put('bool', bool)
    }

    def element = { attrs ->
        if (!attrs.templateDir) {
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

    private Closure datetime = { Map model, Map attrs ->
        gfRender(template: "/goodFormTemplates/$attrs.templateDir/type_datetime", model: model)
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
            gfRender(template: "/goodFormTemplates/$attrs.templateDir/form_bool_wrapper", model: model)
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

    def addAttributes = { attr ->
        Map fieldAttrs = new HashMap(attr.fieldAttr)
        String classString = attr['class']

        if (classString) {
            List classes = fieldAttrs.class ? fieldAttrs.class.split(' ') : []
            classes.add(classString)
            fieldAttrs.class = classes.join(' ')
        }

        if (fieldAttrs['size']) {
            BigDecimal size = new BigDecimal(fieldAttrs.remove('size') as String)
            BigDecimal width = ((size * 0.7) + 2.5).max(4)
            fieldAttrs.style = "max-width: ${width}em"
        }

        List<String> skip = attr.skip ?: []
        List attributeStrings = fieldAttrs.collect { String key, value ->
            if (!(skip && skip.contains(key))) {
                "$key=\"${value.encodeAsHTML()}\""
            }
        }
        out << attributeStrings.join(' ')
    }

    def makeId = { attr ->
        out << (attr.identity.join('-') as String).hashCode()
    }

    def tidy = { attr ->
        Source source = new Source(attr.text as String)
        SourceFormatter sf = source.getSourceFormatter()
        out << sf.toString()
    }

    def renderQuestionSet = { attr ->
        List qSet = attr.qset
        Form questions = attr.questions
        Map formData = attr.data
        Boolean disabled = attr.disabled ?: true
        String templateDir = attr.display ? 'display' : 'input'
        log.debug "in RenderQuestionSet qSet $qSet"
        goodFormService.withQuestions(qSet, questions) { q, qRef ->
            out << element([element: q.formElement, store: formData, disabled: disabled, templateDir: 'display'])
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
        state.eachWithIndex { List<String> qSet, int i ->
            if (found) {
                out << g.render(template: '/goodFormTemplates/common/answeredQuestionSet',
                        model: [id: "$formInstance.id/$i", qSet: qSet, data: formData, questions: questions])
            }
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
        Boolean readOnly = attrs.readOnly
        List state = formInstance.storedState()
        def i = state.size() - 1
        state.each() { List qSet ->
            if (!qSet.isEmpty() && qSet[0] != 'End') {
                out << g.render(template: '/goodFormTemplates/common/displayQuestionSet',
                        model: [id: "$formInstance.id/$i", qSet: qSet, data: formData, questions: questions, readOnly: readOnly])
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
            if (GoodFormService.isCollectionOrArray(flash.message)) {
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
