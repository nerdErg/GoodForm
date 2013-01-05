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

In later steps of the tutorial, we will update this ruleset to include some more complexity.

Now that we have created our ruleset, we can now run our app to see the form in action.

_Next_: [Run the Application](##05-RunApp.md##)