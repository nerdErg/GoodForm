Once the master rule definition has been created, you can then create your ruleset files, which will be installed and made avaiable
from the OneRing instance we setup previously.

There needs to be at least one ruleset file, whose name is 'your-form_FirstQuestion.ruleset', where 'your-form' is the
name of the form definition.

For our example, let's create a `ContactDetails_firstQuestion.ruleset` file in the ~/OneRing/rules/application directory.  The contents of a sample ruleset is listed below:

```groovy
ruleset("ContactDetails") {

    abortOnFail = false

    rule("Get contact details") {
        when {
            true
        }
        then {
            next = ['Q1']
        }
    }
}
```

This rule is saying that when the first question for the Contact Details form is requested, always return 'Q1' as the question to display.  This
ruleset is deliberately simplistic, and the subsequent tutorial pages will demonstrate how we can add more detailed logic within
the ruleset.

We also need to create a ruleset that defines the bahaviour when the form has been submitted.  The ruleset file should be named
'your-formLastQuestion.ruleset', where 'your-form' is the name of your form and 'LastQuestion' is the identifier of the last
question in the form submission.

For our example, let's create a `ContactDetailsQ1.ruleset` in the ~/OneRing/rules/application directory.  The contents of a sample ruleset is listed below:

```groovy
ruleset("ContactDetailsQ1") {

    require(['Q1'])  //we need the answers to this question

    abortOnFail = true

	rule('finished') {
        when {
            true
        }
        then {
            next = ['End']
        }
    }
}
```

This ruleset is saying that when Q1 has been submitted, we can end the form process.

In later steps of the tutorial, we will update these rulesets to include some more complexity.

Now that we have created our ruleset, we can now run our app to see the form in action.

_Next_: [Run the Application](##05-RunApp.md##)