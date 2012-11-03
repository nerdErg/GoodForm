package com.nerderg.goodForm.form

import grails.test.GrailsUnitTestCase

/**
 * User: pmcneil
 * Date: 21/11/11
 *
 */
class FormDSLTests extends GrailsUnitTestCase {

    void testFormElement() {

        FormElement e = new FormElement()
        e.form("This is a test", [pick: 1], null) {
            "are you male?" default: true
            "female"()
            "other"() {
                "well what the heck are you?" text: 80
            }
        }

        printFormElements(e)

        assert e.text == "This is a test"
        assert e.attr == [pick: 1]
        assert e.subElements.size() == 3
        def other = e.subElements.find { it.text == "other" }
        assert other.subElements.size() == 1
        def what = other.subElements[0]
        assert what.attr == [text: 80]

        e = new FormElement()
        e.form("Criminal Record", [pick: 1], null) {
            "No"()
            "Not sure"()
            "Yes"() {
                "details" list: "criminal record", {
                    "year" number: 4
                    "offence" text: 80, hint: "what did you do?"
                    "penalty" text: 120
                }
            }
        }

        printFormElements(e)

        assert e.text == "Criminal Record"
        assert e.attr == [pick: 1]
        assert e.subElements.size() == 3
        def yes = e.subElements.find { it.text == "Yes" }
        assert yes.subElements.size() == 1
        def details = yes.subElements[0]
        assert details.subElements.size() == 3
        assert details.attr == [list: "criminal record"]

    }

    def printFormElements(e, indent = 0) {
        println indentStr("$e.text? $e.attr", indent)
        ++indent
        e.subElements.each { sub ->
            printFormElements(sub, indent)
        }
    }

    private String indentStr(str, indentCount) {
        if (indentCount > 0) {
            int pad = indentCount * 2 + str.size()
            str.padLeft(pad)
        } else {
            return str
        }
    }

    void testQuestion() {
        Question q1 = new Question(ref: "Q1")
        q1.buildQuestion {
            "Criminal Record" pick: 1, {
                "No"()
                "Not sure"()
                "Yes"() {
                    "details" listOf: "criminal record", {
                        "year" number: 4
                        "offence" text: 80, hint: "what did you do?"
                        "penalty" text: 120
                    }
                }
            }
        }

        printFormElements(q1.formElement)

        assert q1.ref == "Q1"
        assert q1.formElement
        assert q1.formElement.text == "Criminal Record"
        assert q1.formElement.attr == [pick: 1]
        assert q1.formElement.subElements.size() == 3
        def yes = q1.formElement.subElements.find { it.text == "Yes" }
        assert yes.subElements.size() == 1
        def details = yes.subElements[0]
        assert details.subElements.size() == 3
        assert details.attr == [listOf: "criminal record"]
    }

    void testForm() {
        Form f = new Form()
        f.question("Q1") {
            "Criminal Record" pick: 1, {
                "No"()
                "Not sure"()
                "Yes"() {
                    "details" listOf: "criminal record", {
                        "year" number: 4
                        "offence" text: 80, hint: "what did you do?"
                        "penalty" text: 120
                    }
                }
            }
        }

        f.question("Q2") {
            "Gender" pick: 1, {
                "are you male?" default: true
                "female"()
                "other"() {
                    "OMG aliens?" text: 80
                }
            }
        }

        f.questions.each { q ->
            printFormElements(q.formElement)
        }
        assert f.questions.size() == 2
        assert f.questions[0].ref == "Q1"
        assert f.questions[1].ref == "Q2"
        FormElement fe2 = f.questions[1].formElement
        assert fe2
        assert fe2.attr == [pick: 1]
        assert fe2.text == "Gender"
        assert fe2.subElements.size() == 3
        def other = fe2.subElements.find { it.text == "other"}
        assert other
        assert other.subElements.size() == 1
        assert other.subElements[0].text == "OMG aliens?"
        assert other.subElements[0].attr == [text:80]
    }

    public class TestUp {
        String a
        String b
        void rename (String a, String b) {
            this.a = a
            this.b = b
        }
    }

    void testUpdate(){

        Form f = new Form()
        f.update('Q1') {
            rename "Q1.the_question_is", "Q1.the_question_was"
        }

        assert f.updates.size() == 1
        TestUp t = new TestUp()
        f.updates.Q1.delegate = t
        f.updates.Q1()
        assert t.a == "Q1.the_question_is"
        assert t.b == "Q1.the_question_was"

    }
}
