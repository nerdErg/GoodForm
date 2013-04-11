package com.nerderg.goodForm

import net.sf.json.JSONArray

import com.nerderg.goodForm.form.Form
import com.nerderg.goodForm.form.FormElement
import com.nerderg.goodForm.form.Question

/**
 * Handles the rendering of form elements.
 * Todo add a sanitised name method for each
 *
 */
class GoodFormService {

    static transactional = false

    static final List knownTypes = ['text', 'date', 'datetime', 'bool', 'pick', 'group', 'listOf', 'money', 'number', 'phone', 'attachment', 'each', 'heading']

    /**
     * compile a form DSL string and return a form instance
     * @param formDefinition
     * @throws InvalidFormDefinitionException thrown if a form does not contain any questions
     * @throws FieldNotFoundException thrown if question does not contain any fields
     * @throws FieldNotMappedException thrown if a field does not contain a 'map' attribute
     * @return
     */
    Form compileForm(String formDefinition) {
        Script dsl = new GroovyShell().parse(formDefinition)
        Form form = processFormScript(dsl, new Form(goodFormService: this))
        return form
    }

    Closure unify = { Closure doit ->
      try {
         doit()
      } catch (GoodFormException e) {
        throw e
      } catch (Exception e) {
        throw new InvalidFormDefinitionException(e)
      }
    }

    /**
     * Process a form DSL script and create a form structure under the formInstance
     * @param dslScript
     * @param formInstance
     * @throws InvalidFormDefinitionException thrown if a form does not contain any questions
     * @throws FieldNotFoundException thrown if question does not contain any fields
     * @throws FieldNotMappedException thrown if a field does not contain a 'map' attribute
     * @return the formInstance
     */
    private Form processFormScript(Script dslScript, Form formInstance) {

        dslScript.metaClass = createEMC(dslScript.class) {
            ExpandoMetaClass emc ->
            emc.form = { Closure formDef ->
                formDef.delegate = formInstance
                formDef.resolveStrategy = Closure.DELEGATE_FIRST
                formDef()
            }
        }
        unify {
            dslScript.run()
            testForm(formInstance)
        }
        return formInstance
    }

    private ExpandoMetaClass createEMC(Class clazz, Closure cl) {

        ExpandoMetaClass emc = new ExpandoMetaClass(clazz, false)
        cl(emc)
        emc.initialize()
        return emc
    }

    /**
     * Performs a sanity check on the structure of the form.  If the form is invalid, then an exception will be thrown.
     * @param form
     * @throws InvalidFormDefinitionException thrown if a form does not contain any questions
     * @throws FieldNotFoundException thrown if question does not contain any fields
     * @throws FieldNotMappedException thrown if a field does not contain a 'map' attribute
     */
    void testForm(Form form) {
        if (!form.questions) {
            throw new InvalidFormDefinitionException("Form must have at least one question")
        }
        form.questions.each {Question q ->
            if (!q.formElement) {
                throw new FieldNotFoundException("Question must define a form element")
            }
            makeElementName(q.formElement)
        }
    }

    /**
     * Traverse the form element structure to get the form that this element belongs to.
     * @param e
     * @return a Form
     */
    Form getFormFromElement(FormElement e) {
        def parent = e
        while (parent.parent) {
            parent = parent.parent
        }
        if (parent instanceof Form) {
            return parent
        }
        return null
    }

    /**
     * Using conventions from the Form DSL create the element name that will be the map reference.
     * @param e
     * @return the elements name
     */
    String makeElementName(FormElement e) {
        String name = ""
        if (e.attr.group) {
            name = e.attr.group
        }
        if (e.attr.listOf) {
            name = e.attr.listOf
        }
        if (e.attr.attachment) {
            name = e.attr.attachment
        }
        if (e.attr.'each') {
            name = e.attr.'each'
        }
        if (e.attr.heading) {
            name = 'heading'
        }
        if (e.attr.map) {
            if (name) {
                name = "${e.attr.map}.${name}"
            } else {
                name = e.attr.map
            }
        }
        if (name) {
            String parentName = e.parent ? e.parent.attr.name : ''
            if (isGrantParentPick1(e)) {
                //if grandparent is a pick 1 we need to move the sub element data to below it in the map
                parentName = parentName.replaceAll(/\.([^\.]*)$/, '_$1')
            }
            return "${parentName}.$name"
        }

        //none of the above, not mapped so should be a radio button that uses the parents name
        if (e.parent && e.parent.attr.pick && e.parent.attr.pick.toString() == "1") {
            return e.parent.attr.name
        }

        throw new FieldNotMappedException(e)
    }

    private isGrantParentPick1(FormElement e) {
        FormElement grandParent = getGrandParent(e)
        return grandParent && grandParent.attr.pick && grandParent.attr.pick.toString() == "1"
    }

    //get the grand parent only if it's a form element
    private FormElement getGrandParent(FormElement e) {
        if (e.parent && e.parent instanceof FormElement && e.parent.parent instanceof FormElement) {
            return e.parent.parent
        }
        return null
    }

    /**
     * filter a user supplied name so it can be used as a map key. The name is usually a value in a list inserted by the
     * rules engine from user input, e.g. a list of children, cars, dogs...
     *
     * @param name
     * @return
     */
    String filterName(String name) {
        String intermediate = name.replaceAll(/[^a-zA-Z 0-9]/, '')
        return intermediate.trim().replaceAll('  *', '_')
    }

    /**
     * Travers the map of maps to the desired field and return the value.
     *
     * @param map the map (of maps) to traverse to get the field
     * @param field a String specifying the field in the map of maps, e.g G2.name.firstName
     * @param index if the value is a list it returns the indexed value
     * @return
     */
    def findField(Map map, String field, Integer index = null) {
        return traverseMapToField(field, map) { lastMap, lastField ->
            def value = lastMap != null ? lastMap[lastField] : null
            return indexedValue(value, index)
        }
    }

    private traverseMapToField(String field, Map map, Closure closure) {
        String[] fieldSplit = field.split(/\./)
        Map lastMap = map
        //traverse to the last, inner most map
        for (int i = 0; i < fieldSplit.size() - 1; i++) {
            String fieldName = fieldSplit[i]
            if (lastMap && lastMap[fieldName] instanceof Map) {
                lastMap = lastMap[fieldName]
            } else {
                lastMap = null
            }
        }
        return closure(lastMap, fieldSplit.last())
    }

    private indexedValue(value, Integer index) {
        if (value != null && index != null) {
            if (value instanceof List || value instanceof Object[] || value instanceof JSONArray) {
                return value[index]
            }
        }
        return value
    }

    /**
     * Traverse the map to the desired field and set its value to value. If the field is not found it will throw a
     * FieldNotFoundException.
     *
     * @param map the map (of maps) to traverse to get the field
     * @param field a String specifying the field in the map of maps, e.g G2.name.firstName
     * @param value the value to set the field
     * @throws FieldNotFoundException
     * @return the value
     */
    def setField(Map map, String field, value) {
        return traverseMapToField(field, map) { Map lastMap, lastField ->
            if (lastMap) {
                lastMap[lastField] = value
            } else {
                throw new FieldNotFoundException("$field not found in $map")
            }
        }
    }

    /**
     * Remove the field specified from the map and return its value. If the field is not found it will throw a
     * FieldNotFoundException.
     *
     * @param map the map (of maps) to traverse to get the field
     * @param field a String specifying the field in the map of maps, e.g G2.name.firstName
     * @throws FieldNotFoundException
     * @return the value of the field removed,
     */
    def removeField(Map map, String field) {
        return traverseMapToField(field, map) { Map lastMap, lastField ->
            if (lastMap) {
                lastMap.remove(lastField)
            } else {
                throw new FieldNotFoundException("$field not found in $map")
            }
        }
    }

    /**
     * Given a form element return it's type as a String from the known types. If the element doesn't have a type specified
     * then it is a boolean type 'bool'
     * @param e
     * @return
     */
    String getElementType(FormElement e) {
        String type = 'bool'
        if (e.attr) {
            List types = e.attr.keySet().intersect(knownTypes) as List
            if (types && types.size() == 1) {
                type = types[0]
            }
        }
        return type
    }

    /**
     * Correctly process "each" form elements' sub elements and calls a closure to do some work with that sub element
     * @param e
     * @param store
     * @param work a closure that is given a map [element: subElement, store: store, index: i]
     * where element is the sub FormElement and index is the index of the source collection item
     * @return void
     */
    void processEachFormElement(FormElement e, Map store, Closure work) {
        String sourceCollectionName = e.attr.'each'
        String pattern = "\\{${sourceCollectionName}\\}"

        def source = store[sourceCollectionName]

        List items = (source instanceof List ? source : [source])

        items.eachWithIndex { String item, i ->
            //because of JSON.Null item may well be null
            if (item) {
                e.subElements.each { FormElement sub ->
                    String subText = sub.text
                    String subMap = sub.attr.map
                    sub.text = sub.text.replaceAll(pattern, item)
                    if (subMap) {
                        sub.attr.map = "${filterName(item)}.${subMap}"
                    } else {
                        sub.attr.map = "${filterName(item)}"
                    }
                    work([element: sub, store: store, index: i])
                    sub.text = subText
                    sub.attr.map = subMap
                }
            }
        }

    }

    /**
     * Takes the params and formats the output as a string
     * @param label - the data label
     * @param value - the data
     * @param units - the units of the data
     * @param indent - a String of spaces(however defined) to use as an indent to represent nesting of questions
     */
    Closure defaultTextOut = { label, value, units, indent ->
        if (value) {
            return "$indent$label : $value ${units ?: ''}\n"
        }
        return "$indent$label : - \n"
    }

    /**
     * print using the closure the form element using the form definition
     * @param e
     * @param answers
     * @param out closure to call to format the values   { label, value, units, indent -> ... }* @return formatted string of the values
     */
    String printFormElementAnswer(FormElement e, Map answers, Closure out) {
        printFormElementAnswer(e, answers, null, '', out)
    }

    String printFormElementAnswer(FormElement e, Map answers, Integer index, String indent, Closure out) {
        String type = getElementType(e)
        "$type"(e, answers, index, indent, out)
    }

    private String heading(FormElement e, Map answers, Integer index, String indent, Closure out) {
        def value = e.attr.heading
        out(e.text, value, e.attr.units, indent, 'heading')
    }

    private String commonText(FormElement e, Map answers, Integer index, String indent, Closure out, String type) {
        e.attr.name = makeElementName(e)
        def value = findField(answers, e.attr.name, index) ?: ''
        out(e.text, value, e.attr.units, indent, type)

    }

    private String text(FormElement e, Map answers, Integer index, String indent, Closure out) {
        commonText(e, answers, index, indent, out, 'text')
    }

    private String date(FormElement e, Map answers, Integer index, String indent, Closure out) {
        commonText(e, answers, index, indent, out, 'date')
    }

    private String number(FormElement e, Map answers, Integer index, String indent, Closure out) {
        commonText(e, answers, index, indent, out, 'number')
    }

    private String phone(FormElement e, Map answers, Integer index, String indent, Closure out) {
        commonText(e, answers, index, indent, out, 'phone')
    }

    private String attachment(FormElement e, Map answers, Integer index, String indent, Closure out) {
        commonText(e, answers, index, indent, out, 'attachment')
    }

    private String datetime(FormElement e, Map answers, Integer index, String indent, Closure out) {
        e.attr.name = makeElementName(e)
        def value = findField(answers, e.attr.name, index) ?: [date: '', time: '']
        out(e.text, "$value.date $value.time", null, indent, 'dateTime')
    }

    private String money(FormElement e, Map answers, Integer index, String indent, Closure out) {
        e.attr.name = makeElementName(e)
        def value = findField(answers, e.attr.name, index)
        out(e.text, value ? "\$$value" : '', e.attr.units, indent, 'money')
    }

    private String bool(FormElement e, Map answers, Integer index, String indent, Closure out) {
        e.attr.name = makeElementName(e)
        def pick = e.parent?.attr?.pick?.toString()
        if (e.subElements.size() > 0) {
            if (pick && pick == "1") {
                def value = findField(answers, e.parent.attr.name, index)
                return radioHiddenSubElements(answers, index, value, e, indent, out)
            } else {
                def value = findField(answers, "${e.attr.name}.yes", index)
                return checkboxHiddenSubElements(answers, index, value, e, indent, out)
            }
        } else {
            if (pick && pick == "1") {
                def value = findField(answers, e.parent.attr.name, index)
                return radioButtonElement(value, e, indent, out)
            } else {
                def value = findField(answers, e.attr.name, index)
                return checkboxElement(value, e, answers, indent, out)
            }
        }
    }

    private String radioHiddenSubElements(Map answers, Integer index, value, FormElement e, String indent, Closure out) {
        if (value == e.text.replaceAll(/'/, '\u2019')) {
            String result = out('', e.text, null, indent, 'radio')
            e.attr.name = makeElementName(e)
            indent += '  '
            e.subElements.each { sub ->
                result += printFormElementAnswer(sub, answers, index, indent, out)
            }
            return result
        } else {
            return ''
        }
    }

    private String checkboxHiddenSubElements(Map answers, Integer index, value, FormElement e, String indent, Closure out) {
        String result = ''
        if (value == 'on') {
            result = out(e.text, 'Yes', null, indent, 'checkbox')
            e.attr.name = makeElementName(e)
            indent += '  '
            e.subElements.each { sub ->
                result += printFormElementAnswer(sub, answers, index, indent, out)
            }
        } else {
            result = out(e.text, 'No', null, indent, 'checkbox')
        }
        return result
    }

    private String radioButtonElement(value, e, String indent, Closure out) {
        if (value == e.text.replaceAll(/'/, '\u2019')) {
            return out('', e.text, null, indent, 'radio')
        }
        return ''
    }

    private String checkboxElement(value, e, answers, String indent, Closure out) {
        if (value == 'on') {
            return out(e.text, 'Yes', null, indent, 'checkbox')
        } else {
            if (e.parent?.attr?.pick) {
                return ''
            }
            return out(e.text, 'No', null, indent, 'checkbox')
        }
    }

    private String pick(FormElement e, Map answers, Integer index, String indent, Closure out) {
        e.attr.name = makeElementName(e)
        String result = out(e.text, ' ', null, indent, 'pick')
        indent += '  '
        e.subElements.each { sub ->
            String res = printFormElementAnswer(sub, answers, index, indent, out)
            if (res) {
                result += res
            }
        }
        return result
    }

    private String group(FormElement e, Map answers, Integer index, String indent, Closure out) {
        e.attr.name = makeElementName(e)
        String result = out(e.text, ' ', null, indent, 'group')
        indent += '  '
        e.subElements.each { sub ->
            result += printFormElementAnswer(sub, answers, index, indent, out)
        }
        return result
    }

    private String each(FormElement e, Map answers, Integer index, String indent, Closure out) {
        String result = ''
        e.attr.name = makeElementName(e)
        processEachFormElement(e, answers) { Map subMap -> //[element: sub, store: store, index: i]
            result += printFormElementAnswer(subMap.element, subMap.store, subMap.index, indent, out)
            result += '\n'
        }
        return result
    }

    private String listOf(FormElement e, Map answers, Integer index, String indent, Closure out) {
        e.attr.name = makeElementName(e)
        int currentListSize = listSize(answers, e.attr.name)
        String result = out(e.text, currentListSize, null, indent, 'listOf')
        indent += '  '
        for (int i = 0; i < Math.max(currentListSize, 1); i++) {
            result += '\n'
            e.subElements.each { sub ->
                result += printFormElementAnswer(sub, answers, i, indent, out)
            }
        }
        return result
    }

    private int listSize(Map store, String listName) {
        def value = findField(store, listName)
        if (value && value instanceof Map) {
            Map.Entry l = value.find { entry ->
                entry.value instanceof List
            }
            return l ? l.value.size() : 0
        } else {
            return 0
        }
    }

    //---- end print formElement support

    /**
     * Print, using the closure, the form Data by question NOT using the form definition
     * @param question a Map of a question generally the sub-map of formData.question
     * @param out closure to call to format the values   { label, value, units, indent -> ... }* @return formatted string of the values
     */
    String printFormDataAnswer(Map question, String indent, Closure out = defaultTextOut) {
        List ignore = ['order', 'yes']
        String result = ''
        if (question) {
            question.each { key, value ->
                if (!ignore.contains(key) && value) {
                    if (value instanceof Map) {
                        if (anyValueSet(value)) {
                            result += out(keyToLabel(key as String), (value.yes ? 'yes' : ' '), null, indent)
                            result += printFormDataAnswer(value, "$indent ", out)
                        }
                    } else {
                        if (value == 'on') {
                            result += out(keyToLabel(key as String), 'yes', null, indent)
                        } else {
                            result += out(keyToLabel(key as String), value, null, indent)
                        }

                    }
                }
            }
        }
        return result
    }

    def anyValueSet(Map map) {
        map.find {
            if (it.value instanceof Map) {
                return anyValueSet(it.value)
            } else {
                return (it.value && it.value != 'none')
            }
        }
    }


    String keyToLabel(String key) {
        key.replaceAll('_', ' ')
    }

    def withQuestions(List<String> qSet, questions, Closure c) {
        qSet.each {qRef ->
            if (qRef != 'End') {
                Question q = questions[qRef]
                if (q) {
                    c(q, qRef)
                } else {
                    log.info "Question $qRef not found"
                }
            }
        }
    }

    /**
     * take a map of lists and invert/pivot to a list of maps
     * So you pass a map that looks like this:
     * <code>
     *     [firstName: ['jim','jane'], lastName: ['smith','jones']]
     * </code>
     * and it returns
     * <code>
     *     [[firstName: 'jim', lastName: 'smith'], [firstName: 'jane', lastName: 'jones']]
     * </code>
     * @param mapOfLists
     * @return listOfMaps
     */
    public List<Map> groupList(Map mapOfLists) {
        List<Map> listOfMaps = []
        mapOfLists.each { e ->
            def value = e.value
            if (value instanceof Map) {
                //if it's a boolean it gets sub mapped to a yes:[..] so flatten
                Map m = value
                if (m.size() == 1) {
                    if (m.yes) {
                        value = m.yes
                    }
                }
            }
            if (value instanceof List) {
                value.eachWithIndex { v, i ->
                    getOrMakeMap(listOfMaps, i)[e.key] = (v == 'on' ? 'yes' : v)
                }
            } else {
                getOrMakeMap(listOfMaps, 0)[e.key] = (value == 'on' ? 'yes' : value)
            }
        }
        return listOfMaps
    }

    private Map getOrMakeMap(List<Map> source, int index) {
        if (source.size() > index) {
            return source[index]
        } else {
            Map n = [:]
            source.add(n)
            return n
        }
    }
}
