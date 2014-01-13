package com.nerderg.goodForm

import com.nerderg.goodForm.form.Form
import com.nerderg.goodForm.form.FormElement
import com.nerderg.goodForm.form.Question
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Handles the rendering of form elements.
 * Todo add a sanitised name method for each
 *
 */
class GoodFormService {

    static transactional = false

    /**
     * A map of known element types.
     * Types are mapped to a closure that extracts relevant information about the type for display as a field in summary
     * or as an input.
     *
     * the closure must take (FormElement e, Map answers, Integer index) and return a model.
     * The model by convention has a submap called fieldAttributes which contains the field value and field specific
     * attributes. Your closure should call getDefaultModelProperties with a map of extra fieldAttributes
     * Map model = getDefaultModelProperties(e, answers, index, disabled, [myattribute : e.attr.myspecialthing])
     *
     * if you only need the default attributes just add the defaultAttributes closure.
     * @see GoodFormService
     */
    static final Map<String, Closure> elementTypeModel = [:]

    GoodFormService() {
        initDefaultElements()
    }

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
                    formDef.resolveStrategy = DELEGATE_FIRST
                    formDef()
                }
        }
        unify {
            dslScript.run()
            testForm(formInstance)
        }
        return formInstance
    }

    private static ExpandoMetaClass createEMC(Class clazz, Closure cl) {

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
        form.questions.each { Question q ->
            visitAllFormElements(q.formElement) { FormElement formElement ->
                formElement.attr.name = makeElementName(formElement)
            }
        }
    }

    /**
     * Do the closure work on the passed in formElement and all it's subElements.
     * The closure is passed in the individual formElement.
     * @param formElement the form element to start at.
     * @param work the closure for the work to be done.
     * @throws FieldNotFoundException thrown if question does not contain any fields
     */
    void visitAllFormElements(FormElement formElement, Closure work) {
        if (!formElement) {
            throw new FieldNotFoundException("Question must define a form element")
        }
        work(formElement)
        formElement.subElements.each { FormElement subElement ->
            visitAllFormElements(subElement, work)
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

    private static isGrantParentPick1(FormElement e) {
        FormElement grandParent = getGrandParent(e)
        return grandParent && grandParent.attr.pick && grandParent.attr.pick.toString() == "1"
    }

    //get the grand parent only if it's a form element
    private static FormElement getGrandParent(FormElement e) {
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
     * Traverse the map of maps to the desired field and return the value.
     *
     * @param map the map (of maps) to traverse to get the field
     * @param field a String specifying the field in the map of maps, e.g G2.name.firstName
     * @param index if the value is a list it returns the indexed value
     * @return
     */
    def findField(Map map, String field, Integer index = null) {
        return traverseMapToField(field, map) { Map lastMap, String lastField ->
            def value = lastMap != null ? lastMap[lastField] : null
            return indexedValue(value, index)
        }
    }

    private static traverseMapToField(String field, Map map, Closure closure) {
        String[] fieldSplit = field.split(/\./)
        Map lastMap = map
        //traverse to the last, inner most map
        for (int i = 0; i < fieldSplit.size() - 1; i++) {
            String fieldName = fieldSplit[i]
            if (lastMap && lastMap[fieldName] instanceof Map) {
                lastMap = lastMap[fieldName] as Map
            } else {
                lastMap = null
            }
        }
        return closure(lastMap, fieldSplit.last())
    }

    private static indexedValue(value, Integer index) {
        if (value != null && index != null) {
            if (value instanceof List || value instanceof Object[] || value instanceof JSONArray) {
                return (value as List)[index]
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
            List types = e.attr.keySet().intersect(elementTypeModel.keySet()) as List
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
                    FormElement clone = copyElement(sub)
                    clone.text = sub.text.replaceAll(pattern, item)
                    String map = clone.attr.map
                    if (map) {
                        clone.attr.map = "${filterName(item)}.${map}"
                    } else {
                        clone.attr.map = "${filterName(item)}"
                    }
                    clone.attr.name = makeElementName(clone)
                    work([element: clone, store: store, index: 0])
                }
            }
        }

    }

    /**
     * Get the minimum and maximum value attributes from a number formElement
     * @param formElement
     * @return map [max: max, min: min]
     */
    Map getNumberMinMax(FormElement formElement) {

        Map minMax = [:]
        //todo make non specific to number input (use range?)
        if (formElement.attr.number instanceof Range) {
            minMax.max = formElement.attr.number.to as BigDecimal
            minMax.min = formElement.attr.number.from as BigDecimal
        } else {
            //note avoid groovy truth for number 0 check for null. Note null attributes aren't added to tags in nerdergFormTags
            if (formElement.attr.max != null) {
                minMax.max = formElement.attr.max
            }
            if (formElement.attr.min != null) {
                minMax.min = formElement.attr.min
            }
        }
        return minMax
    }

    /**
     * This makes a shallow copy of the FormElement. It shallow copies the attributes map and just copies the reference
     * to the subElements.
     *
     * @param orig
     * @return
     */
    private static FormElement copyElement(FormElement orig) {
        FormElement clone = new FormElement(orig.qref)
        clone.text = orig.text
        clone.parent = orig.parent
        clone.qref = orig.qref
        clone.subElements = orig.subElements
        clone.attr = new LinkedHashMap(orig.attr)
        return clone
    }

    private static getFieldErrors(Map formData, String field, Integer index) {
        return formData.fieldErrors[field + (index == null ? '0' : index)]
    }

    Map getDefaultModelProperties(FormElement e, Map store, Integer index, boolean disabled, Map fieldAttributes) {
        def value = findField(store, e.attr.name as String, index) ?: (e.attr.default ?: '')
        String type = getElementType(e)
        String error = getFieldErrors(store, e.attr.name as String, index)

        if (e.attr.suggest) {
            if (fieldAttributes.class) {
                fieldAttributes.class += " suggest ${e.attr.suggest}"
            } else {
                fieldAttributes.class = "suggest ${e.attr.suggest}"
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

    private Map makeHtmlAttributes(boolean disabled, FormElement e, String type) {

        Map fieldAttributes = getNumberMinMax(e) //get min/max if exist
        fieldAttributes << makeFieldSizeAttributes(e, type)

        if (disabled) {
            fieldAttributes << [disabled: 'disabled']
        }

        if (e.attr.required) {
            fieldAttributes << [required: 'required']
        }

        fieldAttributes << getPattern(e)

        return fieldAttributes
    }

    Map getPattern(FormElement e) {
        if (e.attr.pattern) {
            if (isCollectionOrArray(e.attr.pattern)) {
                List pat = e.attr.pattern as List
                if (pat.size() == 2) {
                    return [pattern: pat[0], title: pat[1]]
                } else {
                    return [pattern: e.attr.pattern]
                }
            }
            if (e.attr.pattern instanceof Map) {
                Map pat = e.attr.pattern as Map
                return pat
            }
            return [pattern: e.attr.pattern]
        }
        return [:]
    }

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

    void addFormElementType(String name, Closure c) {
        elementTypeModel.put(name, c)
    }

    void initDefaultElements() {
        addFormElementType('text', defaultAttributesModel)
        addFormElementType('number', defaultAttributesModel)
        addFormElementType('phone', defaultAttributesModel)
        addFormElementType('money', defaultAttributesModel)
        addFormElementType('group', defaultAttributesModel)
        addFormElementType('pick', defaultAttributesModel)
        addFormElementType('each', defaultAttributesModel)

        addFormElementType('select', selectModel)
        addFormElementType('heading', headingModel)
        addFormElementType('attachment', attachmentModel)
        addFormElementType('date', dateModel)
        addFormElementType('datetime', datetimeModel)
        addFormElementType('bool', boolModel)
        addFormElementType('listOf', listOfModel)
    }

    Map getElementModel(Map attrs) {
        FormElement e = attrs.element
        Map store = attrs.store
        Integer index = attrs.index ?: 0
        Boolean disabled = attrs.disabled ?: false
        getElementModel(e, store, index, disabled)
    }

    Map getElementModel(FormElement e, Map answers, Integer index, Boolean disabled) {
        String type = getElementType(e)
        log.debug "*** geting model for $type"
        return elementTypeModel[type](e, answers, index, disabled)
    }

    private Closure headingModel = { FormElement e, Map answers, Integer index, Boolean disabled ->
        getDefaultModelProperties(e, answers, index, disabled, [size: e.attr.heading])
    }

    private Closure defaultAttributesModel = { FormElement e, Map answers, Integer index, Boolean disabled ->
        getDefaultModelProperties(e, answers, index, disabled, [:])
    }

    private Closure selectModel = { FormElement e, Map answers, Integer index, Boolean disabled ->
        getDefaultModelProperties(e, answers, index, disabled, [options: e.attr.select as List])
    }

    private Closure attachmentModel = { FormElement e, Map answers, Integer index, Boolean disabled ->
        Map model = getDefaultModelProperties(e, answers, index, disabled, [:])
        List<String> fieldSplit = (e.attr.name as String).split(/\./)
        Integer prefix = "${fieldSplit[0]}.${fieldSplit.last()}-".size()
        String value = model.fieldAttributes.value
        String fileName = (value && value.size() > prefix) ? value.substring(prefix) : ''
        model.fieldAttributes.fileName = fileName
        if (fileName && model.fieldAttributes.required) {
            model.fieldAttributes.remove('required')
        }
        return model
    }

    private Closure dateModel = { FormElement e, Map answers, Integer index, Boolean disabled ->
        String format = e.attr.date
        getDefaultModelProperties(e, answers, index, disabled, [format: format, size: format.size()])
    }

    private Closure datetimeModel = { FormElement e, Map answers, Integer index, Boolean disabled ->
        String format = e.attr.datetime
        getDefaultModelProperties(e, answers, index, disabled, [format: format, size: format.size()])
    }

    private Closure listOfModel = { FormElement e, Map answers, Integer index, Boolean disabled ->
        Map model = getDefaultModelProperties(e, answers, index, disabled, [:])
        model.listSize = listSize(model.fieldAttributes.value)
        return model
    }

    private Closure boolModel = { FormElement e, Map answers, Integer index, Boolean disabled ->

        Boolean pick1 = '1' == e.parent?.attr?.pick?.toString()
        Map model = getDefaultModelProperties(e, answers, index, disabled, [pick1: pick1, parentName: e.parent.attr.name])
        if (model.label == model.fieldAttributes.value) {
            model.fieldAttributes.checked = 'checked'
        }

        if (e.subElements.size() > 0) {
            if (!pick1) {
                String answer = findField(answers, "${e.attr.name}.yes", index)
                if (answer) {
                    //need to get the renamed value as the name space isn't nested. which affects if it is checked
                    model.fieldAttributes.checked = 'checked'
                } else {
                    if (model.fieldAttributes.checked) {
                        model.fieldAttributes.remove('checked')
                    }
                }
            }
        }
        return model
    }

    private static int listSize(value) {
        if (value && value instanceof Map) {
            Map.Entry l = value.find { entry ->
                (entry.value instanceof List)
            }
            return l ? l.value.size() : 0
        } else {
            return 0
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
                return anyValueSet(it.value as Map)
            } else {
                return (it.value && it.value != 'none')
            }
        }
    }


    String keyToLabel(String key) {
        key.replaceAll('_', ' ')
    }

    def withQuestions(List<String> qSet, Form questions, Closure c) {
        qSet.each { qRef ->
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

    private static Map getOrMakeMap(List<Map> source, int index) {
        if (source.size() > index) {
            return source[index]
        } else {
            Map n = [:]
            source.add(n)
            return n
        }
    }

    /**
     * Checks that the object is a List or Array or Set.
     * This will return false for a Map
     * @param obj
     * @return true if this is not a string but a collection
     */
    boolean isCollectionOrArray(obj) {
        (obj instanceof Collection || obj instanceof Object[])
    }

}
