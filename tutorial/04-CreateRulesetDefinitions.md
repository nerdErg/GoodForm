Create Ruleset Definitions
===

Once the master rule definition has been created, you can then create your ruleset files, which will be installed and made available
from the OneRing instance we setup previously.

There needs to be at least one ruleset file, whose name is 'your-form_FirstQuestion.ruleset', where 'your-form' is the
name of the form definition.

For our example, let's create a `ContactDetails_firstQuestion.ruleset` file in the ~/OneRing/rules/application directory.
The contents of a sample ruleset is listed below:

```groovy
ruleset("ContactDetails") {

    abortOnFail = false

    rule("Get contact details") {
        when {
            true
        }
        then {
            next = ['Q1', 'Q2']
        }
    }
}
```

[Click here to see the change](https://github.com/rossrowe/GoodForm-Tutorial/compare/tutorial-step3-f...tutorial-step4-a)

Let's look at this file line by line...

```groovy
ruleset("ContactDetails") {
```

The line defines the name of the ruleset.  The name should match the name of the Form Definition defined in the previous page
(ie. 'ContactDetails).

```groovy
   abortOnFail = false
```

This tells OneRing to continue processing if the form processing fails.

```groovy
   rule("Get contact details") {
```

This defines a rule for the ruleset.  The name of the rule can be anything you like, in this case we've named it 'Get contact details'.

```groovy
when {
    true
}
then {
    next = ['Q1', 'Q2']
}
```

This is the actual rule to process when determining the questions to be displayed.  In this case, it's very simple, it's saying to
always return Q1 and Q2 as the questions to display for this ruleset.

This ruleset is saying that when the first question for the Contact Details form is requested, always return 'Q1' and 'Q2' as the questions to display.  This
ruleset is deliberately simplistic, and the subsequent tutorial pages will demonstrate how we can add more detailed logic within
the ruleset.

We also need to create a ruleset that defines the behaviour when the form has been submitted.  The ruleset file should be named
'[your-form][LastQuestion].ruleset', where 'your-form' is the name of your form and 'LastQuestion' is the identifier of the last
question in the form submission.

For our example, let's create a `ContactDetailsQ2.ruleset` in the ~/OneRing/rules/application directory.  The contents of a sample ruleset is listed below:

```groovy
ruleset("ContactDetailsQ2") {

    require(['Q1'])  //we need the answers to this question

    abortOnFail = true

	rule('finished') {
        when {
            true
        }
        then {
            next = ['End']
            description = "$Q1.names.givenNames $Q1.names.lastName"
        }
    }
}
```

[Click here to see the change](https://github.com/rossrowe/GoodForm-Tutorial/compare/tutorial-step4-a...tutorial-step4-b)

This ruleset is saying that when Q1 has been submitted, we can end the form process.

It also sets the description (from the form data) for this form instance which is displayed by default in the list of
forms in the index of your form controller.

We also need a rule called ContactDetailsCheckRequiredDocuments that looks something like this:

```groovy
ruleset("ContactDetailsCheckRequiredDocuments") {

    require()  //we need the answers to these questions

    rule('prep') {
        fact.require = []  //create the set 'require'
    }

    rule('Is colour indicated') {
        when {
            Q2 && !Q2.faveColour
        }
        then {
            require.add([Q: 'Q2', message: 'You must have a fave colour'])
        }
    }
}
```

This rule is a final check of our form for documents or questions that may have been missed. You can make it so your
form may have documents you need to attach, however you don't want to prevent people from filling in as much as they
can. This rule lets you check what is missing and indicate it in the 'require' set.

In later steps of the tutorial, we will update these rulesets to include some more complexity.

Now that we have created our ruleset, we can now run our app to see the form in action.

_Next_: [Run the Application](05-RunApp.md)