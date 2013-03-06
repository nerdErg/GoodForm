# Install Plugin

We can now install the GoodForm plugin within a Grails project.

If you don't already have a Grails project, you can create one by running:

    grails create-app goodform_tutorial

This will create a `goodform_tutorial` directory containing the default Grails source files.

For the purposes of this tutorial, we are going to continue referencing to the `goodform_tutorial` Grails project.

We now need to include the GoodForm plugin within the goodform_tutorial project.  To do this, update the grails-app/conf/BuildConfig.groovy file to include the following::

```groovy
grails.project.dependency.resolution = {
//snip
    plugins {
        //snip

        compile ':rendering:0.4.3'
        compile ':goodform:1.0'
        compile ':simple-suggestions:0.1'
    }
}
```

We have also included the Simple Suggestions plugin which provides an autocomplete services that can be used by GoodForm.

#Add Form Definition(s) to Bootstrap

Once the plugin has been installed, we can now create the form definition.  As we want the form definition to be present
every time the application is run, we include it in the `grails-app/conf/BootStrap.groovy` file.

The form definition should include the complete set of questions you wish to be included in your form.  We can control the
visibility of the questions through the rulesets, which will be covered later.

```groovy
import com.nerderg.goodForm.FormDefinition

class BootStrap {

        def init = { servletContext ->

            String sampleForm = """
                form {  //start with a 'form' element
                   question("Q1") {   //include a 'question' element with an identifier
                       "What is your name?" group: "names", {
                           "Title" text: 10, hint: "e.g. Mr, Mrs, Ms, Miss, Dr", suggest: "title", map: 'title'
                           "Given Names" text: 50, required: true, map: 'givenNames'
                           "Last or Family Name" text: 50, required: true, map: 'lastName'
                           "Have you been or are you known by any other names?" hint: "e.g. maiden name, previous married name, alias, name at birth", map: 'hasAlias', {
                               "List your other names" listOf: "aliases", {
                                   "Other name" text: 50, map: 'alias'
                                   "Type of name" text: 40, hint: "e.g maiden name", suggest: "nameType", map: 'aliasType'
                               }
                           }
                       }
                   }
                   question("Q2") { //second question
                       "What is your favorite colour?" text: 20, suggest: "colour", map: 'faveColour'
                   }
            }"""


            if (!FormDefinition.get(1)) {
                FormDefinition formDefinition = new FormDefinition(name: 'ContactDetails', formDefinition: sampleForm, formVersion: 1)
                formDefinition.save()
            }

}
```

Put a text file with colour names in a folder called suggestions. [See Suggestions Plugin] (http://nerderg.com/Simple+Suggestions+plugin)

This is a very simple form with just two questions ('What is your name?' and 'What is your favorite colour?' ).
However, the form demonstrates some key features of Goodform.

#Naming convention

The basic structure of a form definition in GoodForm is as follows:

```groovy
form {
   question("some_id") {
      "A field"
      "Another field"
  }
}
```

All form definitions must start with a `form` element.  Within this `form`, one or more `question` elements can be defined.  And
within each `question` one or more fields can be defined.

The structure of a field is as follows:

    "Field Label" field_type:field_length, hint: "Field Label Hint", map: "Field variable name"

eg.
    "Name" text: 10, hint: "Your name goes here", map: "name", required: "true"

The `map` attribute is required for all fields (except `group` and `list` fields).  If a map attribute is not defined, then
a FieldNotMappedException will be thrown when the form is rendered.

![Field Not Mapped](##field-not-mapped.png##)

#Sub-fields

Sub-fields can defined within fields, eg.

```groovy
"Have you been or are you known by any other names?" hint: "e.g. maiden name, previous married name, alias, name at birth", map: 'hasAlias', {
   "List your other names" listOf: "aliases", {
       "Other name" text: 50, map: 'alias'
       "Type of name" text: 40, hint: "e.g maiden name", suggest: "nameType", map: 'aliasType'
   }
}
```

#Field types

GoodForm supports the following field types 'out-of-the-box':

* Text
* Number
* Date
* Headings
* File attachments
* Money
* Check-boxes and Radio buttons (booleans)

Hints can be added to fields by including a 'hint' attribute, eg

    "Title" text: 10, hint: "e.g. Mr, Mrs, Ms, Miss, Dr", map: 'title'

#Validation - field length, mandatory

Fields can be marked as mandatory by specifying a 'required' attribute, eg.

    "Given Names" text: 50, required: true, map: 'givenNames'

#Multi-values

Repeating groups of fields can be specified using the 'listOf' attribute, eg.

    "List your other names" listOf: "aliases", {
       "Other name" text: 50, map: 'alias'
       "Type of name" text: 40, hint: "e.g maiden name", suggest: "nameType", map: 'aliasType'
    }

#Suggest entry

The Simple Suggestions plugin includes support for predictive text suggestions.  This can be implemented by defining a
field with a 'suggest' attribute, eg.

    "What is your favorite colour?" text: 20, suggest: "colour", map: 'faveColour'

You can register a suggestion handler matching the value of the suggest attribute, eg.

```
suggestService.addSuggestionHandler('colour') { String term ->
                File colorsNames = new File('colourNames.txt')
                List<String> colours = []
                colorsNames.eachLine { colours.add(it.toString())}
                String q = term.toUpperCase()
                return colours.findAll { it.toUpperCase().contains(q) }
            }
```

or you can just stick a text file with colour names on each line in the suggestions folder.

---

The [goodform example]() source code includes two sample form definitions defined in the `BootStrap.groovy` file.  The first
form is a simple form intended to be displayed on a single page, which only contains three questions.  The second form is
more complex, and is intended to be displayed over several pages.

#Create a Controller

Now that we have populated our FormDefinition, we will also create a controller within our example grails application which
will handle web requests for the form. By convention the form controller needs to end in `FormController`.

To do this, run the following command:

    grails create-controller ContactDetailsForm

This will create a `ContactDetailsFormController` class within the `goodform-example/grails-app/controllers` directory.

Update this class to extend from the `com.nerderg.goodForm.FormController` class, eg.

```groovy
import com.nerderg.goodForm.FormController

class ContactDetailsFormController extends FormController {

    def createForm() {
        createForm('ContactDetails')
    }

}
```
Forms can be served up without the controller, we just need to reference `/goodform_tutorial/form/createForm?formName='ContactDetails'`
However, having a specific controller allows you to specify custom behaviour during the form lifecycle operations (eg.
send an email/generate a PDF), which we will cover later.

#Update layout

Update the `grails-app\views\layouts\main.gsp` to include the following lines

```
    <r:require modules="goodForm"/>
```

This line should be included before the <g:layoutHead/> line.

#Reference OneRing location

The last thing we need to do is tell our Grails application where the OneRing web service is located.  To do this, we
add the following to the `grails-app/conf/Config.groovy` file:

```groovy
environments {
    production {

    }
    test {

    }
    development {
        rulesEngine.uri = 'http://localhost:8081/rulesEngine'
    }
}
```

Now that we have setup our Grails project, let's create the ruleset definitions.

_Next_: [Create Ruleset Definitions](##04-CreateRulesetDefinitions.md##)