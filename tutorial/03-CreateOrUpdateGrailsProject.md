We now need to install the goodform plugin within a Grails project.

If you don't already have a Grails project, you can create one by running:

    grails create-app goodform_tutorial

For the purposes of this tutorial, we are going to keep refering to the `goodform_tutorial` Grails project.

This will create a `goodform_tutorial` directory containing the default Grails source files.

We now need to install the GoodForm Grails plugin within the Grails project.  To do this, change to the `goodform_tutorial` directory and run the following:

    grails install-plugin goodform 1.0.0-SNAPSHOT

#TODO change version number

Once the plugin has been installed, we can now create the form definition.  As we want the form definition to be present
every time the application is run, we include it in the `grails-app/conf/BootStrap.groovy` file.

The form definition should include the complete set of questions you wish to be included in your form.  We can control the
visibility of the questions through the rulesets, which will be covered later.

```groovy
import com.nerderg.goodForm.FormDefinition

class BootStrap {

        def init = { servletContext ->

            String sampleForm = """ form {  //start with a 'form' element
                   question("Q1") {   //include a 'question' element with an identifier
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
}
```

This is a very simple form with just one question ('what is your name?').  However, the form does demonstrate some key
features of Goodform.

#Sub-questions
#Field types
#Hints
#Validation - field length, mandatory
#Multi-values

The [goodform example]() source code includes two sample form definitions defined in the `BootStrap.groovy` file.  The first
form is a simple form intended to be displayed on a single page, which only contains three questions.  The second form is
more complex, and is intended to be displayed over several pages.

Now that we have populated our FormDefinition, we will also create a controller within our example grails application which
will handle web requests for the form.

To do this, run the following command:

    grails create-controller ContactDetails

This will create a `ContactDetailsController` class within the `goodform-example/grails-app/controllers` directory.

Update this class to extend from the `com.nerderg.goodForm.FormController` class, eg.

```groovy
import com.nerderg.goodForm.FormController

class ContactDetailsController extends FormController {

    def index {}

}
```
#TODO forms can be served up without the controller, we just need to reference localhost:8080/goodform_tutorial/form...
#However, having a specific controller allows you to specify custom behaviour during the form lifecycle operations (eg.
#send an email/generate a PDF), which we will cover later.

_Next_: [Create Ruleset Definitions](##04-CreateRulesetDefinitions.md##)