* Once the master rule list has been created, you can now create your ruleset files, which will be installed and made avaiable
from the OneRing instance we setup previously.

* There needs to be at least one ruleset file, whose name is 'your-form_FirstQuestion.ruleset', where 'your-form' is the
name of the form definition.

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

    test(:) {
        next(['Q1', 'Q2'])
    }
}
```

