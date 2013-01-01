Goodform HOWTO

* Install and setup <a href="">OneRing</a> according to the instructions at
* Create ruleset file(s) and deploy to OneRing instance (eg. ~/.OneRing).  There needs to be a .ruleset file named:

      formname_firstQuestion.ruleset

  where formname is the name of your form.

* Additional form pages should be defined in files named:

      formnameQUESTION#.ruleset

  where formname is the name of your form and QUESTION# is the question reference for the first question to be displayed on the form.

* The form definition should be included in the BootStrap.groovy file.  This should be the entire set of questions for the form,
and should be stored in a FormDefinition instance, eg.

    class BootStrap {

        def init = { servletContext ->

            String sampleForm = """ form {
                   question("Q1") {
                       "What is your name?" group: "names", {
                           "Title" text: 10, hint: "e.g. Mr, Mrs, Ms, Miss, Dr", suggest: "title"
                           "Given Names" text: 50, required: true
                           "Last or Family Name" text: 50, required: true
                           "Have you been or are you known by any other names?" hint: "e.g. maiden name, previous married name, alias, name at birth", {
                               "List your other names" listOf: "aliases", {
                                   "Other name" text: 50
                                   "Type of name" text: 40, hint: "e.g maiden name", suggest: "nameType"
                               }
                           }
                       }
                   }
            }"""

            if (!FormDefinition.get(1)) {
                FormDefinition formDefinition = new FormDefinition(name: 'SampleForm', formDefinition: sampleForm, formVersion: 1)
                formDefinition.save()
            }

* To link to a form, include
    <g:link controller="form" action="createForm" params="[formName: 'your_form_name']">Link Text Here</g:link>
